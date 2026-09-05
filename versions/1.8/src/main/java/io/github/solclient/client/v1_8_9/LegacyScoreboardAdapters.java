package io.github.solclient.client.v1_8_9;

import io.github.solclient.client.platform.mc.text.Text;
import io.github.solclient.client.platform.mc.world.scoreboard.Objective;
import io.github.solclient.client.platform.mc.world.scoreboard.PlayerTeam;
import io.github.solclient.client.platform.mc.world.scoreboard.Score;
import io.github.solclient.client.platform.mc.world.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;

public final class LegacyScoreboardAdapters {

    private LegacyScoreboardAdapters() {
    }

    public static Objective objective(net.minecraft.scoreboard.ScoreboardObjective objective) {
        return objective == null ? null : new LegacyObjective(objective);
    }

    private static final class LegacyObjective implements Objective {
        private final net.minecraft.scoreboard.ScoreboardObjective objective;

        private LegacyObjective(net.minecraft.scoreboard.ScoreboardObjective objective) {
            this.objective = objective;
        }

        @Override
        public Scoreboard getScoreboard() {
            return new LegacyScoreboard(objective.getScoreboard());
        }

        @Override
        public Text getDisplayName() {
            return Text.literal(objective.getDisplayName());
        }
    }

    private static final class LegacyScoreboard implements Scoreboard {
        private final net.minecraft.scoreboard.Scoreboard scoreboard;

        private LegacyScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard) {
            this.scoreboard = scoreboard;
        }

        @Override
        public Collection<Score> getScores(Objective objective) {
            if (!(objective instanceof LegacyObjective)) {
                return java.util.Collections.emptyList();
            }

            Collection<Score> scores = new ArrayList<>();
            for (net.minecraft.scoreboard.ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(
                    ((LegacyObjective) objective).objective)) {
                scores.add(new LegacyScore(score));
            }
            return scores;
        }

        @Override
        public PlayerTeam getPlayersTeam(String playerName) {
            return new LegacyPlayerTeam(scoreboard.getPlayerTeam(playerName));
        }
    }

    private static final class LegacyScore implements Score {
        private final net.minecraft.scoreboard.ScoreboardPlayerScore score;

        private LegacyScore(net.minecraft.scoreboard.ScoreboardPlayerScore score) {
            this.score = score;
        }

        @Override
        public String getOwner() {
            return score.getPlayerName();
        }

        @Override
        public int getValue() {
            return score.getScore();
        }

        @Override
        public String getPlayerName() {
            return score.getPlayerName();
        }
    }

    private static final class LegacyPlayerTeam implements PlayerTeam {
        private final net.minecraft.scoreboard.Team team;

        private LegacyPlayerTeam(net.minecraft.scoreboard.Team team) {
            this.team = team;
        }

        @Override
        public Text formatText(String text, boolean numbers) {
            return Text.literal(team == null ? text : team.decorateName(text));
        }
    }
}
