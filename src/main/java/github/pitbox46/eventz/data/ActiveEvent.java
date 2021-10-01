package github.pitbox46.eventz.data;

import github.pitbox46.eventz.Eventz;
import github.pitbox46.eventz.ServerEvents;
import github.pitbox46.eventz.data.contestant.EventContestant;
import github.pitbox46.eventz.data.contestant.PlayerContestant;
import github.pitbox46.eventz.data.contestant.ServerContestant;
import github.pitbox46.eventz.data.contestant.TeamContestant;
import github.pitbox46.monetamoney.data.Ledger;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.annotation.Nullable;
import java.util.*;

public class ActiveEvent {
    public long startTime;
    public final Event event;
    public final List<EventContestant> contestantList = new ArrayList<>(8);
    public ScoreObjective scoreboardObjective;
//    public FluidStack fluid;
    //-1 == no energy
//    public int energy;
    public boolean checkTimedCondition = true;


    public ActiveEvent(Event event) {
        this.event = event;
    }

    public void start(PlayerList playerList) {
        IFormattableTextComponent title = new StringTextComponent(event.title).mergeStyle(TextFormatting.LIGHT_PURPLE);
        IFormattableTextComponent subtitle = new StringTextComponent(event.description).mergeStyle(TextFormatting.DARK_PURPLE);
        ServerEvents.sendGlobalMsg(title.deepCopy().appendString("\n").appendSibling(subtitle));
        Eventz.getServer().getPlayerList().sendPacketToAllPlayers(new STitlePacket(STitlePacket.Type.TITLE, title, 20, 60, 20));
        Eventz.getServer().getPlayerList().sendPacketToAllPlayers(new STitlePacket(STitlePacket.Type.SUBTITLE, subtitle, 20, 60, 20));
        for(ServerPlayerEntity player: Eventz.getServer().getPlayerList().getPlayers()) {
            player.connection.sendPacket(new SPlaySoundEffectPacket(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.BLOCKS, player.getPosX(), player.getPosY(), player.getPosZ(), 64.0F, 1.0F));
        }
        event.startScript();

        Scoreboard scoreboard = Eventz.getServer().getScoreboard();
        scoreboard.setObjectiveInDisplaySlot(0, null);
        scoreboard.setObjectiveInDisplaySlot(1, null);
        scoreboard.setObjectiveInDisplaySlot(2, null);

        scoreboardObjective = new ScoreObjective(scoreboard, "eventz", ScoreCriteria.DUMMY, new TranslationTextComponent("title.eventz.scoreboard"), ScoreCriteria.RenderType.INTEGER);

        if(event.gates.isEmpty()) {
            stop();
        } else {
            if(event.type == Event.Type.INDIVIDUAL) {
                for (ServerPlayerEntity player : playerList.getPlayers()) {
                    contestantList.add(new PlayerContestant(player));
                }
            }
            else if(event.type == Event.Type.TEAM) {
                for(ServerPlayerEntity player : playerList.getPlayers()) {
                    Team team = Teams.getPlayersTeam(Teams.jsonFile, player.getGameProfile().getName());
                    if(contestantList.stream().noneMatch(c -> ((TeamContestant) c).team.equals(team)))
                        contestantList.add(new TeamContestant(team, Collections.singletonList(player)));
                    else {
                        contestantList.forEach(c -> {
                            if(c instanceof TeamContestant && ((TeamContestant) c).team.equals(team)) {
                                ((TeamContestant) c).players.add(player);
                            }
                        });
                    }
                }
            }
            else {
                contestantList.add(new ServerContestant());
            }
            EventGate firstGate = event.gates.get(0);
            contestantList.forEach(c -> c.onStartGate(firstGate));
            firstGate.enable();
        }

        startTime = System.currentTimeMillis();
    }

    public void addLateEntry(EventContestant contestant) {
        contestantList.add(contestant);
        updateGatesIndividual(contestant, 0);
    }

    public void tick() {
        if(System.currentTimeMillis() >= startTime + (event.duration * 60000)) {
            ServerEvents.sendGlobalMsg(new TranslationTextComponent("message.eventz.eventexpired"));
            stop();
        }
        if(checkTimedCondition && ServerEvents.tick % 20 == 3) {
            contestantList.forEach(c -> c.conditions.forEach((key, value) -> {
                if (!value.getRight() && value.getLeft().endTime != 0 && value.getLeft().endTime <= System.currentTimeMillis()) {
                    value.getLeft().timesUp();
                }
            }));
        }
        if(ServerEvents.tick % 20 == 5) updateGatesAndCheckWinners();
    }

    public void trigger(EventContestant triggeredContestant, String triggerName, Object... params) {
        checkTimedCondition = false;
        int gateNumber = triggeredContestant.gateNumber;
        EventGate gate = event.gates.get(gateNumber);
        Condition condition = triggeredContestant.conditions.get(triggerName).getLeft();

        List<Object> paramList = new ArrayList<>(params.length + 2);

        paramList.add(condition.contestantData.getOrDefault(triggeredContestant, condition.defaultObject));
        paramList.add(condition.globalData);
        paramList.add(triggeredContestant.getName()); //The name of the contestant
        paramList.addAll(Arrays.asList(params)); //Trigger specific info

        JSObject returnValue = condition.trigger(paramList);
        condition.contestantData.put(triggeredContestant, returnValue);
        if(returnValue != null) {
            checkTimedCondition = true;
            if (returnValue.getMember("meta_data") instanceof JSObject) {
                JSObject currentGateMetaData = (JSObject) returnValue.getMember("meta_data");
                if (currentGateMetaData.hasMember("completed") && currentGateMetaData.getMember("completed").equals(true)) {
                    triggeredContestant.conditions.get(triggerName).setRight(true);
                    if (gate.operator == EventGate.Operator.OR) {
                        completeGate(triggeredContestant, gateNumber);
                    }
                    // Operator == AND
                    else {
                        if (triggeredContestant.conditions.values().stream().allMatch(MutablePair::getRight)) {
                            completeGate(triggeredContestant, gateNumber);
                        }
                    }
                }

            }
            if (returnValue.getMember("global_data") instanceof JSObject) {
                JSObject globalData = condition.globalData = (JSObject) returnValue.getMember("global_data");
                //Todo This
                Scoreboard scoreboard = Eventz.getServer().getScoreboard();
                if (globalData.hasMember("scoreboard")) {
                    ScoreboardData data = ScoreboardData.readFromJSObject((JSObject) globalData.getMember("scoreboard"));
                    for (Map.Entry<String, Integer> entry : data.scores.entrySet()) {
                        ScorePlayerTeam team = scoreboard.getTeam(entry.getKey());
                        if (team == null) {
                            team = scoreboard.createTeam(entry.getKey());
                            scoreboard.addPlayerToTeam(entry.getKey(), team);
                        }
                        scoreboard.getOrCreateScore(entry.getKey(), scoreboardObjective).setScorePoints(entry.getValue());
                    }
                    scoreboard.setObjectiveInDisplaySlot(1, scoreboardObjective);
                } else {
                    scoreboard.setObjectiveInDisplaySlot(1, null);
                }
            }
        }
    }

    public void completeGate(EventContestant contestant, int gateNumber) {
        EventGate oldGate = event.gates.get(gateNumber);
        contestant.iterate();
        if (contestant.gateNumber == event.gates.size()) {
            finishEvent(contestant);
        } else {
            EventGate newGate =  event.gates.get(gateNumber + 1);
            contestant.onStartGate(newGate);
            newGate.enable();
        }
        oldGate.onComplete();
    }

    public void updateGatesAndCheckWinners() {
        //Update gates
        for(int i = 0; i < event.gates.size(); i++) {
            if(event.gates.get(i).globalCompleted) {
                for(EventContestant contestant: contestantList) {
                    if(contestant.gateNumber == i) {
                        contestant.iterate();
                        //Check to make sure this is not the last gate before using onStartGate()
                        if (contestant.gateNumber < event.gates.size()) {
                            EventGate newGate = event.gates.get(i + 1);
                            contestant.onStartGate(newGate);
                            newGate.enable();
                        }
                    }
                }
            }
        }

        // Check winners
        List<EventContestant> winnerList = new ArrayList<>();
        for(EventContestant contestant: contestantList) {
            if(contestant.gateNumber == event.gates.size()) {
                winnerList.add(contestant);
            }
        }
        if(winnerList.size() > 0)
            finishEvent(winnerList.toArray(new EventContestant[0]));
    }

    public void updateGatesIndividual(EventContestant contestant, int gateNumber) {
        for(int i = gateNumber; i < event.gates.size(); i++) {
            if(event.gates.get(i).globalCompleted) {
                contestant.iterate();
                //Check to make sure this is not the last gate before using onStartGate()
                if (contestant.gateNumber < event.gates.size()) {
                    EventGate newGate = event.gates.get(i + 1);
                    contestant.onStartGate(newGate);
                    newGate.enable();
                }
            } else {
                break;
            }
        }
    }


    public void finishEvent(EventContestant... contestants) {
        StringBuilder winMessage = new StringBuilder(); //Used to append names of winners to message
        for(EventContestant contestant: contestants) {
            if(contestant.getClass() == PlayerContestant.class) {
                ServerPlayerEntity player = ((PlayerContestant) contestant).player;
                if(player != null) {
                    String playerName = player.getGameProfile().getName();
                    Ledger.addBalance(Ledger.jsonFile, playerName, event.monetaReward);
                    player.inventory.placeItemBackInInventory(player.getEntityWorld(), event.itemReward);
                    winMessage.append(playerName).append(", ");
                }
            } else if(contestant.getClass() == TeamContestant.class) {
                Team team = ((TeamContestant) contestant).team;
                team.balance += event.monetaReward;
                Teams.updateTeam(Teams.jsonFile, team);
                ServerPlayerEntity randomPlayer = Eventz.getServer().getPlayerList().getPlayers().stream().filter(player -> team.members.contains(player.getGameProfile().getName())).findAny().get();
                randomPlayer.inventory.placeItemBackInInventory(randomPlayer.getEntityWorld(), event.itemReward);

                team.members.forEach(s -> winMessage.append(s).append(", "));
            } else if(contestant.getClass() == ServerContestant.class) {
                for(ServerPlayerEntity player: Eventz.getServer().getPlayerList().getPlayers()) {
                    if(player != null) {
                        String playerName = player.getGameProfile().getName();
                        Ledger.addBalance(Ledger.jsonFile, playerName, event.monetaReward);
                        player.inventory.placeItemBackInInventory(player.getEntityWorld(), event.itemReward);
                        winMessage.append(playerName).append(", ");
                    }
                }
                break; //Should only have one contestant
            } else {
                throw new RuntimeException("Unrecognized class: " + contestant.getClass());
            }
        }
        //Remove the last ", "
        if(winMessage.length() > 0) {
            winMessage.delete(winMessage.length() - 2, winMessage.length() - 1);
        }

        ServerEvents.sendGlobalMsg(new TranslationTextComponent("message.eventz.eventcomplete", winMessage.toString()));
        stop();
    }

    public EventContestant getContestant(String name) {
        return getContestant(Eventz.getServer().getPlayerList().getPlayerByUsername(name));
    }

    @Nullable
    public EventContestant getContestant(ServerPlayerEntity player) {
        switch(event.type) {
            case TEAM: {
                for(EventContestant contestant: contestantList) {
                    if(((TeamContestant) contestant).players.contains(player)) {
                        return contestant;
                    }
                }
                // Search for player's team and add them to it
                Team team = Teams.getPlayersTeam(Teams.jsonFile, player.getGameProfile().getName());
                if(!team.isNull()) {
                    for(EventContestant contestant: contestantList) {
                        TeamContestant teamContestant = (TeamContestant) contestant;
                        if(teamContestant.team.members.contains(player.getGameProfile().getName())) {
                            teamContestant.players.add(player);
                            return contestant;
                        }
                    }
                    addLateEntry(new TeamContestant(team, Collections.singletonList(player)));
                }
            } break;
            case SERVER: {
                return contestantList.get(0);
            }
            case INDIVIDUAL: {
                for(EventContestant contestant: contestantList) {
                    if(((PlayerContestant) contestant).player.equals(player)) {
                        return contestant;
                    }
                }
                addLateEntry(new PlayerContestant(player));
            } break;
            default: {
                throw new RuntimeException("No case for enum");
            }
        }
        return null;
    }

    public void stop() {
        Eventz.activeEvent = null;
    }
}
