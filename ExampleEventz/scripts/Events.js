function tameFiveWolves(previousValues, globalData, contestantName, uuid, playerName, entityType) {
    if(entityType === "minecraft:wolf") {
        var count = !(previousValues === null) && !isNaN(previousValues.wolfCount) ? previousValues.wolfCount + 1 : 1;
        if(count == 2) {
            return {
                "meta_data": {
                    "completed":true
                }
            };
        }
        return {
            "meta_data": {
                "completed": false
            },
            "wolfCount": count
        };
    }
    return { 
        "meta_data": {
            "completed": false
        },
        "wolfCount": count
    };
}

function randomSmeltTest(previousValues, globalData, contestantName, uuid, playerName) {
    return {
        "meta_data": {
            "completed": true
        }
    };
}

function interfaceBlockTest(previousValues, globalData, contestantName, fluid, fluidAmount, energy, item, itemAmount) {
    if(item === "minecraft:stone") {
        var count = !(previousValues === null) && !isNaN(previousValues.itemCount) ? previousValues.itemCount + itemAmount : itemAmount;
        if(count >= 5) {
            return {
                "meta_data": {
                    "completed": true
                }
            };
        } else {
            return {
                "meta_data": {
                    "completed": false
                },
                "itemCount": count
            };
        }
    }
    return {
        "meta_data": {
            "completed": false
        },
        "itemCount": previousValues === null ? 0 : previousValues.itemCount
    };
}

function foodTimerExample(previousValues, globalData, contestantName, uuid, playerName, food, hungerRestore, saturation) {
    if(!(globalData === null) && globalData.time_up) {
        var winningContestant;
        var winningCount = 0;
        for(const contestant in globalData.contestantMap) {
            if(globalData.contestantMap.contestant > winningCount) {
                winningContestant = contestant;
                winningCount = globalData.contestantMap.contestant;
            }
        }
        return {
            "global_data": {
                "winners": [
                    winningContestant
                ]
            }
        }
    }
    if(saturation > 0) {
        if(globalData === null) {
            return {
                "global_data": {
                    "contestantMap": {
                        contestantName: saturation
                    }
                }
            };
        } else {
            if(!(globalData.contestantMap === null) && isNaN(globalData.contestantMap.contestantName)) {
                globalData.contestantMap.contestantName = saturation;
            } else {
                globalData.contestantMap.contestantName += saturation;
            }
            return {
                "global_data": globalData
            }
        }
    }
}