package com.presetmanager;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecommendationCatalogTest {
    @Test public void dismissalsAreEncounterSpecific() {
        RecommendationCatalog.Encounter tob = RecommendationCatalog.encounters().get(0);
        RecommendationCatalog.Encounter inferno = RecommendationCatalog.encounters().get(1);
        Set<String> dismissed = new HashSet<>();
        dismissed.add(RecommendationCatalog.key(tob.id, "tile-indicators"));
        assertEquals(1, RecommendationCatalog.visible(tob, dismissed).size());
        assertEquals(1, RecommendationCatalog.visible(inferno, dismissed).size());
    }

    @Test public void dismissalsRoundTrip() {
        Set<String> dismissed = new HashSet<>();
        dismissed.add("theatre-of-blood:tile-indicators");
        dismissed.add("inferno:tile-indicators");
        assertEquals(dismissed, RecommendationCatalog.parseDismissals(
            RecommendationCatalog.serializeDismissals(dismissed)));
    }

    @Test public void encounterIdsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (RecommendationCatalog.Encounter encounter : RecommendationCatalog.encounters()) {
            assertTrue("Duplicate encounter: " + encounter.id, ids.add(encounter.id));
            assertFalse(encounter.recommendations.isEmpty());
            Set<String> recommendationIds = new HashSet<>();
            for (RecommendationCatalog.Recommendation recommendation : encounter.recommendations) {
                assertTrue("Duplicate suggestion: " + recommendation.id,
                    recommendationIds.add(recommendation.id));
            }
        }
    }

    @Test public void dismissingOneEncounterDoesNotHideAnother() {
        Set<String> dismissed = new HashSet<>();
        dismissed.add(RecommendationCatalog.key("theatre-of-blood", "ground-markers"));
        RecommendationCatalog.Encounter chambers = RecommendationCatalog.encounters().stream()
            .filter(e -> "chambers-of-xeric".equals(e.id)).findFirst().get();
        assertEquals(2, RecommendationCatalog.visible(chambers, dismissed).size());
    }

    @Test public void invalidDismissalEntriesAreIgnored() {
        assertEquals(1, RecommendationCatalog.parseDismissals(
            "theatre-of-blood:tile-indicators\n../invalid\n").size());
    }
}
