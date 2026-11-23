package com.presetmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Local recommendation catalog. Entries are editorial suggestions, not popularity rankings.
 * Plugin IDs must be verified against Plugin Hub before a recommendation is published.
 */
final class RecommendationCatalog {
    static final class Recommendation {
        final String id;
        final String name;
        final String description;
        final String pluginHubId;
        Recommendation(String id, String name, String description, String pluginHubId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.pluginHubId = pluginHubId;
        }
    }
    static final class Encounter {
        final String id;
        final String name;
        final String category;
        final List<Recommendation> recommendations;
        Encounter(String id, String name, String category, Recommendation... recommendations) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.recommendations = Collections.unmodifiableList(Arrays.asList(recommendations));
        }
        @Override public String toString() { return name; }
    }

    private static final List<Encounter> ENCOUNTERS = Collections.unmodifiableList(Arrays.asList(
        new Encounter("theatre-of-blood", "Theatre of Blood", "Raids",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights your current tile and destination for precise movement.", null),
            new Recommendation("ground-markers", "Ground Markers",
                "Mark useful positions for rooms and encounters.", null)),
        new Encounter("inferno", "Inferno", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Makes tile movement easier to track.", null)),
        new Encounter("colosseum", "Fortis Colosseum", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Makes tile movement easier to track.", null)),
        new Encounter("chambers-of-xeric", "Chambers of Xeric", "Raids",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null),
            new Recommendation("ground-markers", "Ground Markers",
                "Mark useful positions during encounters.", null)),
        new Encounter("tombs-of-amascut", "Tombs of Amascut", "Raids",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null),
            new Recommendation("ground-markers", "Ground Markers",
                "Mark useful positions during encounters.", null)),
        new Encounter("doom-of-mokhaiotl", "Doom of Mokhaiotl", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null)),
        new Encounter("zulrah", "Zulrah", "PvM",
            new Recommendation("ground-markers", "Ground Markers",
                "Mark useful positions during the encounter.", null)),
        new Encounter("vorkath", "Vorkath", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null)),
        new Encounter("gauntlet", "The Gauntlet", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null)),
        new Encounter("nightmare", "The Nightmare", "PvM",
            new Recommendation("tile-indicators", "Tile Indicators",
                "Highlights movement and tile positioning.", null))
    ));

    static List<Encounter> encounters() { return ENCOUNTERS; }

    static List<Recommendation> visible(Encounter encounter, Set<String> dismissed) {
        if (encounter == null) return Collections.emptyList();
        List<Recommendation> result = new ArrayList<>();
        for (Recommendation recommendation : encounter.recommendations) {
            if (!dismissed.contains(key(encounter.id, recommendation.id))) result.add(recommendation);
        }
        return result;
    }

    static String key(String encounterId, String recommendationId) {
        return encounterId + ":" + recommendationId;
    }

    static Set<String> parseDismissals(String raw) {
        Set<String> result = new HashSet<>();
        if (raw == null || raw.isEmpty()) return result;
        for (String line : raw.split("\\n")) {
            if (line.matches("[a-z0-9-]+:[a-z0-9-]+")) result.add(line);
        }
        return result;
    }

    static String serializeDismissals(Set<String> dismissed) {
        List<String> sorted = new ArrayList<>(dismissed);
        Collections.sort(sorted);
        return String.join("\n", sorted);
    }

    private RecommendationCatalog() {}
}
