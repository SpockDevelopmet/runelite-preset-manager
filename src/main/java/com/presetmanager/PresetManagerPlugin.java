package com.presetmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;

/**
 * Selective configuration snapshots. Does not start or stop other plugins.
 * Work in progress: requires build and Plugin Hub API validation.
 */
@PluginDescriptor(name = "Preset Manager", description = "Selective RuneLite configuration presets",
    tags = {"presets", "pvm", "profiles"})
public class PresetManagerPlugin extends Plugin {
    private static final String GROUP = "selectivepresetmanager";
    private static final String PRESETS = "presets";
    private static final String RECOVERY = "recovery";
    private static final String ACTIVE = "active";
    private static final String DISMISSED = "dismissedRecommendations";
    private static final int MAX_PRESETS = 250;
    private static final int MAX_SETTINGS = 300;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final Type presetType = new TypeToken<List<Preset>>() {}.getType();
    private final Type recoveryType = new TypeToken<Map<String, String>>() {}.getType();
    @Inject private ConfigManager config;
    @Inject private ClientToolbar toolbar;
    private List<Preset> presets = new ArrayList<>();
    private Map<String, String> recovery;
    private String active;
    private boolean recoveryCorrupt;
    private boolean presetsCorrupt;
    private Set<String> dismissedRecommendations = new HashSet<>();
    private NavigationButton navigation;
    private ManagerPanel panel;

    @Override protected void startUp() {
        dismissedRecommendations = RecommendationCatalog.parseDismissals(config.getConfiguration(GROUP, DISMISSED));
        String saved = config.getConfiguration(GROUP, PRESETS);
        if (saved != null) {
            try {
                List<Preset> parsed = gson.fromJson(saved, presetType);
                validateList(parsed);
                presets = parsed;
            } catch (RuntimeException ignored) {
                // Keep corrupt storage intact for manual recovery; never overwrite it automatically.
                presets = new ArrayList<>();
                presetsCorrupt = true;
            }
        }
        String pending = config.getConfiguration(GROUP, RECOVERY);
        if (pending != null) {
            try {
                recovery = gson.fromJson(pending, recoveryType);
                if (recovery == null) throw new IllegalArgumentException("Invalid recovery snapshot");
                for (String key : recovery.keySet()) splitKey(key);
                active = config.getConfiguration(GROUP, ACTIVE);
            } catch (RuntimeException ex) {
                // Block new activations if recovery data is damaged.
                recovery = null;
                recoveryCorrupt = true;
                active = "Recovery needs attention";
            }
        }
        SwingUtilities.invokeLater(() -> {
            panel = new ManagerPanel();
            BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = icon.createGraphics();
            g.setColor(new java.awt.Color(89, 164, 133));
            g.fillRoundRect(1, 1, 14, 14, 4, 4);
            g.setColor(java.awt.Color.WHITE);
            g.drawLine(4, 5, 12, 5);
            g.drawLine(4, 8, 12, 8);
            g.drawLine(4, 11, 10, 11);
            g.dispose();
            navigation = NavigationButton.builder().tooltip("Preset Manager").icon(icon)
                .priority(7).panel(panel).build();
            toolbar.addNavigation(navigation);
        });
    }

    @Override protected void shutDown() {
        if (navigation != null) {
            NavigationButton button = navigation;
            SwingUtilities.invokeLater(() -> toolbar.removeNavigation(button));
        }
    }

    private static String joined(String group, String key) { return group + "\u0000" + key; }

    private static String[] splitKey(String value) {
        String[] parts = value.split("\u0000", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()
            || GROUP.equals(parts[0])) throw new IllegalArgumentException("Invalid recovery key");
        return parts;
    }

    private static void validatePreset(Preset preset) {
        if (preset == null || preset.name == null || preset.name.trim().isEmpty()
            || preset.name.length() > 100 || preset.tag == null || preset.tag.length() > 100
            || preset.settings == null || preset.settings.size() > MAX_SETTINGS)
            throw new IllegalArgumentException("Invalid preset metadata");
        Set<String> seen = new HashSet<>();
        for (Setting s : preset.settings) {
            if (s == null || s.group == null || s.key == null || s.group.trim().isEmpty()
                || s.key.trim().isEmpty() || s.group.indexOf('\u0000') >= 0
                || s.key.indexOf('\u0000') >= 0 || GROUP.equals(s.group)
                || !seen.add(joined(s.group, s.key)))
                throw new IllegalArgumentException("Invalid, duplicate or protected setting");
        }
    }

    private static void validateList(List<Preset> list) {
        if (list == null || list.size() > MAX_PRESETS)
            throw new IllegalArgumentException("Invalid preset collection");
        for (Preset p : list) validatePreset(p);
    }

    private void persist() {
        if (presetsCorrupt) throw new IllegalStateException("Saved presets are damaged; refusing to overwrite them");
        config.setConfiguration(GROUP, PRESETS, gson.toJson(presets));
    }

    private void activate(Preset preset) {
        validatePreset(preset);
        if (preset.settings.isEmpty()) throw new IllegalArgumentException("Preset has no settings");
        if (presetsCorrupt) throw new IllegalStateException("Saved presets are damaged; activation is blocked");
        if (recoveryCorrupt) throw new IllegalStateException("Recovery snapshot is damaged. Back up your RuneLite configuration before changing anything.");
        if (recovery != null) throw new IllegalStateException("Restore previous settings first");
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (Setting s : preset.settings)
            snapshot.put(joined(s.group, s.key), config.getConfiguration(s.group, s.key));
        config.setConfiguration(GROUP, RECOVERY, gson.toJson(snapshot));
        config.setConfiguration(GROUP, ACTIVE, preset.name);
        recovery = snapshot;
        active = preset.name;
        try {
            for (Setting s : preset.settings) write(s.group, s.key, s.value);
        } catch (RuntimeException ex) {
            try { restore(); } catch (RuntimeException restoreError) { ex.addSuppressed(restoreError); }
            throw ex;
        }
    }

    private void write(String group, String key, String value) {
        if (value == null) config.unsetConfiguration(group, key);
        else config.setConfiguration(group, key, value);
    }

    private void restore() {
        if (recoveryCorrupt) throw new IllegalStateException("Recovery snapshot is damaged; refusing to discard it");
        if (recovery == null) return;
        for (Map.Entry<String, String> entry : recovery.entrySet()) {
            String[] parts = splitKey(entry.getKey());
            write(parts[0], parts[1], entry.getValue());
        }
        config.unsetConfiguration(GROUP, RECOVERY);
        config.unsetConfiguration(GROUP, ACTIVE);
        recovery = null;
        active = null;
    }

    private void exportPresets() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("runelite-presets.json"));
        if (chooser.showSaveDialog(panel) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path path = chooser.getSelectedFile().toPath();
            if (Files.exists(path) && JOptionPane.showConfirmDialog(panel,
                "Overwrite this file?", "Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            Files.write(path, gson.toJson(presets).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { panel.error(e.getMessage()); }
    }

    private void importPresets() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path path = chooser.getSelectedFile().toPath();
            if (Files.size(path) > 1024 * 1024) throw new IllegalArgumentException("File exceeds 1 MB");
            List<Preset> incoming = gson.fromJson(
                new String(Files.readAllBytes(path), StandardCharsets.UTF_8), presetType);
            validateList(incoming);
            if (presetsCorrupt) throw new IllegalStateException("Saved presets are damaged; back up your configuration before importing");
            if (JOptionPane.showConfirmDialog(panel,
                "Replace " + presets.size() + " presets with " + incoming.size()
                + " imported presets? Only import trusted files. Settings will not be applied.",
                "Import", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            presets = incoming;
            persist();
            panel.refresh();
        } catch (Exception e) { panel.error(e.getMessage()); }
    }

    private static final class Setting {
        String group, key, value;
        Setting(String group, String key, String value) {
            this.group = group; this.key = key; this.value = value;
        }
        @Override public String toString() { return group + "." + key + " = " + value; }
    }

    private static final class Preset {
        String name, tag;
        List<Setting> settings = new ArrayList<>();
        Preset(String name, String tag) { this.name = name; this.tag = tag; }
        @Override public String toString() { return name + " [" + tag + "]"; }
    }

    private final class ManagerPanel extends PluginPanel {
        private final JComboBox<Preset> picker = new JComboBox<>();
        private final JLabel status = new JLabel();
        private final JComboBox<RecommendationCatalog.Encounter> encounterPicker = new JComboBox<>();
        private final JTextField presetSearch = new JTextField();
        private final JTextField encounterSearch = new JTextField();
        private final JPanel recommendationRows = new JPanel(new GridLayout(0, 1, 3, 3));
        ManagerPanel() {
            super(false);
            setLayout(new BorderLayout(6, 6));
            JPanel controls = new JPanel(new GridLayout(0, 1, 4, 4));
            controls.add(new JLabel("SELECTIVE PRESETS"));
            presetSearch.setToolTipText("Filter your saved presets by name or tag");
            presetSearch.putClientProperty("JTextField.placeholderText", "Search my presets");
            presetSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            });
            controls.add(presetSearch);
            controls.add(picker);
            JButton create = new JButton("Create preset");
            create.addActionListener(e -> {
                JTextField name = new JTextField();
                JTextField tag = new JTextField("Bossing");
                JPanel form = new JPanel(new GridLayout(0, 1));
                form.add(new JLabel("Name")); form.add(name);
                form.add(new JLabel("Tag")); form.add(tag);
                if (JOptionPane.showConfirmDialog(this, form, "New preset",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                if (presets.size() >= MAX_PRESETS) { error("Preset limit reached"); return; }
                Preset p = new Preset(name.getText().trim(), tag.getText().trim());
                try { validatePreset(p); presets.add(p); persist(); refresh(); picker.setSelectedItem(p); }
                catch (RuntimeException ex) { presets.remove(p); error(ex.getMessage()); }
            });
            controls.add(create);
            JButton rename = new JButton("Rename / change tag");
            rename.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null) return;
                JTextField name = new JTextField(p.name);
                JTextField tag = new JTextField(p.tag);
                JPanel form = new JPanel(new GridLayout(0, 1));
                form.add(new JLabel("Name")); form.add(name);
                form.add(new JLabel("Tag")); form.add(tag);
                if (JOptionPane.showConfirmDialog(this, form, "Edit preset",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                String oldName = p.name, oldTag = p.tag;
                p.name = name.getText().trim();
                p.tag = tag.getText().trim();
                try { validatePreset(p); persist(); refresh(); picker.setSelectedItem(p); }
                catch (RuntimeException ex) {
                    p.name = oldName; p.tag = oldTag; error(ex.getMessage());
                }
            });
            controls.add(rename);
            JButton duplicate = new JButton("Duplicate selected preset");
            duplicate.addActionListener(e -> {
                Preset original = (Preset) picker.getSelectedItem();
                if (original == null) return;
                if (presets.size() >= MAX_PRESETS) { error("Preset limit reached"); return; }
                String name = JOptionPane.showInputDialog(this, "Name for copy", original.name + " copy");
                if (name == null) return;
                Preset copy = new Preset(name.trim(), original.tag);
                for (Setting setting : original.settings)
                    copy.settings.add(new Setting(setting.group, setting.key, setting.value));
                try { validatePreset(copy); presets.add(copy); persist(); refresh(); picker.setSelectedItem(copy); }
                catch (RuntimeException ex) { presets.remove(copy); error(ex.getMessage()); }
            });
            controls.add(duplicate);
            JButton add = new JButton("Add configuration setting");
            add.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null) return;
                JTextField group = new JTextField();
                JTextField key = new JTextField();
                JTextField value = new JTextField();
                JPanel form = new JPanel(new GridLayout(0, 1));
                form.add(new JLabel("Configuration group")); form.add(group);
                form.add(new JLabel("Configuration key")); form.add(key);
                form.add(new JLabel("Raw value")); form.add(value);
                if (JOptionPane.showConfirmDialog(this, form, "Add setting",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                Setting setting = new Setting(group.getText().trim(), key.getText().trim(), value.getText());
                p.settings.add(setting);
                try { validatePreset(p); persist(); refresh(); picker.setSelectedItem(p); }
                catch (RuntimeException ex) { p.settings.remove(setting); error(ex.getMessage()); }
            });
            controls.add(add);
            JButton capture = new JButton("Capture current value");
            capture.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null || p.settings.isEmpty()) return;
                Object chosen = JOptionPane.showInputDialog(this, "Capture the current RuneLite value for:",
                    "Capture setting", JOptionPane.PLAIN_MESSAGE, null, p.settings.toArray(), null);
                if (!(chosen instanceof Setting)) return;
                Setting setting = (Setting) chosen;
                String current = config.getConfiguration(setting.group, setting.key);
                if (current == null && JOptionPane.showConfirmDialog(this,
                    "This setting is currently unset. Save an unset value?",
                    "Capture", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                setting.value = current;
                persist();
            });
            controls.add(capture);
            JButton removeSetting = new JButton("Remove saved setting");
            removeSetting.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null || p.settings.isEmpty()) return;
                Object selected = JOptionPane.showInputDialog(this, "Select setting",
                    "Remove setting", JOptionPane.PLAIN_MESSAGE, null, p.settings.toArray(), null);
                if (selected != null && JOptionPane.showConfirmDialog(this, "Remove setting?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    p.settings.remove(selected);
                    try { persist(); } catch (RuntimeException ex) { p.settings.add((Setting) selected); error(ex.getMessage()); }
                }
            });
            controls.add(removeSetting);
            JButton edit = new JButton("Edit saved setting");
            edit.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null || p.settings.isEmpty()) return;
                Object selected = JOptionPane.showInputDialog(this, "Choose a setting",
                    "Edit setting", JOptionPane.PLAIN_MESSAGE, null, p.settings.toArray(), null);
                if (!(selected instanceof Setting)) return;
                Setting original = (Setting) selected;
                JTextField value = new JTextField(original.value == null ? "" : original.value);
                int choice = JOptionPane.showConfirmDialog(this, value,
                    "New value (leave blank for empty string)", JOptionPane.OK_CANCEL_OPTION);
                if (choice != JOptionPane.OK_OPTION) return;
                String previous = original.value;
                original.value = value.getText();
                try { validatePreset(p); persist(); }
                catch (RuntimeException ex) { original.value = previous; error(ex.getMessage()); }
            });
            controls.add(edit);
            JButton activateButton = new JButton("Activate selected preset");
            activateButton.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p == null) return;
                try { activate(p); refreshStatus(); } catch (RuntimeException ex) { error(ex.getMessage()); }
            });
            controls.add(activateButton);
            JButton restoreButton = new JButton("Restore previous settings");
            restoreButton.addActionListener(e -> {
                try { restore(); refreshStatus(); } catch (RuntimeException ex) { error(ex.getMessage()); }
            });
            controls.add(restoreButton);
            JButton delete = new JButton("Delete selected preset");
            delete.addActionListener(e -> {
                Preset p = (Preset) picker.getSelectedItem();
                if (p != null && JOptionPane.showConfirmDialog(this, "Delete " + p.name + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    presets.remove(p);
                    try { persist(); refresh(); } catch (RuntimeException ex) { presets.add(p); error(ex.getMessage()); }
                }
            });
            controls.add(delete);
            JButton export = new JButton("Export presets");
            export.addActionListener(e -> exportPresets());
            controls.add(export);
            JButton imp = new JButton("Import presets");
            imp.addActionListener(e -> importPresets());
            controls.add(imp);
            controls.add(status);
            controls.add(new JLabel("ENCOUNTER RECOMMENDATIONS"));
            encounterSearch.setToolTipText("Search encounters");
            encounterSearch.putClientProperty("JTextField.placeholderText", "Search bosses and raids");
            encounterSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshEncounters(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshEncounters(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshEncounters(); }
            });
            controls.add(encounterSearch);
            refreshEncounters();
            encounterPicker.addActionListener(e -> refreshRecommendations());
            controls.add(encounterPicker);
            JButton createFromEncounter = new JButton("Create preset for this encounter");
            createFromEncounter.addActionListener(e -> {
                RecommendationCatalog.Encounter encounter =
                    (RecommendationCatalog.Encounter) encounterPicker.getSelectedItem();
                if (encounter == null) return;
                if (presets.size() >= MAX_PRESETS) { error("Preset limit reached"); return; }
                String name = JOptionPane.showInputDialog(this, "Preset name", encounter.name);
                if (name == null) return;
                Preset preset = new Preset(name.trim(), encounter.category);
                try {
                    validatePreset(preset);
                    presets.add(preset);
                    persist();
                    refresh();
                    picker.setSelectedItem(preset);
                    JOptionPane.showMessageDialog(this,
                        "Preset created. Add only the settings you want this preset to control. "
                        + "Suggestions are not automatically installed or applied.",
                        "Preset created", JOptionPane.INFORMATION_MESSAGE);
                } catch (RuntimeException ex) {
                    presets.remove(preset);
                    error(ex.getMessage());
                }
            });
            controls.add(createFromEncounter);
            controls.add(new JLabel("Curated suggestions, not popularity rankings"));
            controls.add(recommendationRows);
            JButton resetDismissals = new JButton("Show dismissed suggestions");
            resetDismissals.addActionListener(e -> {
                dismissedRecommendations.clear();
                config.unsetConfiguration(GROUP, DISMISSED);
                refreshRecommendations();
            });
            controls.add(resetDismissals);
            add(new JScrollPane(controls), BorderLayout.CENTER);
            refreshRecommendations();
            refresh();
        }
        private void refresh() {
            Preset selected = (Preset) picker.getSelectedItem();
            picker.removeAllItems();
            String query = presetSearch.getText().trim().toLowerCase(java.util.Locale.ROOT);
            for (Preset p : presets) {
                if (p.name.toLowerCase(java.util.Locale.ROOT).contains(query)
                    || p.tag.toLowerCase(java.util.Locale.ROOT).contains(query)) picker.addItem(p);
            }
            if (selected != null) {
                for (int i = 0; i < picker.getItemCount(); i++) {
                    if (picker.getItemAt(i) == selected) {
                        picker.setSelectedIndex(i);
                        break;
                    }
                }
            }
            refreshStatus();
        }
        private void refreshEncounters() {
            RecommendationCatalog.Encounter selected =
                (RecommendationCatalog.Encounter) encounterPicker.getSelectedItem();
            String query = encounterSearch.getText().trim().toLowerCase(java.util.Locale.ROOT);
            encounterPicker.removeAllItems();
            for (RecommendationCatalog.Encounter encounter : RecommendationCatalog.encounters()) {
                if (encounter.name.toLowerCase(java.util.Locale.ROOT).contains(query)
                    || encounter.category.toLowerCase(java.util.Locale.ROOT).contains(query))
                    encounterPicker.addItem(encounter);
            }
            if (selected != null) {
                for (int i = 0; i < encounterPicker.getItemCount(); i++) {
                    if (encounterPicker.getItemAt(i) == selected) {
                        encounterPicker.setSelectedIndex(i);
                        break;
                    }
                }
            }
            refreshRecommendations();
        }
        private void refreshRecommendations() {
            recommendationRows.removeAll();
            RecommendationCatalog.Encounter encounter =
                (RecommendationCatalog.Encounter) encounterPicker.getSelectedItem();
            if (encounter != null) {
                for (RecommendationCatalog.Recommendation recommendation :
                    RecommendationCatalog.visible(encounter, dismissedRecommendations)) {
                    JPanel row = new JPanel(new BorderLayout(3, 3));
                    JLabel description = new JLabel("<html><b>" + recommendation.name
                        + "</b><br>" + recommendation.description + "</html>");
                    row.add(description, BorderLayout.CENTER);
                    JButton dismiss = new JButton("Not for me");
                    dismiss.setToolTipText("Hide this suggestion for this encounter");
                    dismiss.addActionListener(e -> {
                        dismissedRecommendations.add(RecommendationCatalog.key(
                            encounter.id, recommendation.id));
                        config.setConfiguration(GROUP, DISMISSED,
                            RecommendationCatalog.serializeDismissals(dismissedRecommendations));
                        refreshRecommendations();
                    });
                    row.add(dismiss, BorderLayout.SOUTH);
                    recommendationRows.add(row);
                }
            }
            if (recommendationRows.getComponentCount() == 0)
                recommendationRows.add(new JLabel("No suggestions to show"));
            recommendationRows.revalidate();
            recommendationRows.repaint();
        }
        private void refreshStatus() {
            status.setText(presetsCorrupt ? "Saved presets damaged - edits blocked" : recoveryCorrupt ? "Recovery damaged - activation blocked" : recovery == null ? "No preset active" : "Active: " + active);
        }
        private void error(String message) {
            JOptionPane.showMessageDialog(this, message, "Preset Manager", JOptionPane.ERROR_MESSAGE);
        }
    }
}
