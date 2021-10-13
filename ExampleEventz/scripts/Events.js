function tameFiveWolves(previousValues, globalData, contestantName, uuid, playerName, entityType) {
    if(entityType === globalData["startData"]["0"]["tame_mob"]["mob"]) {
        var scoreboard = globalData !== undefined && globalData["scoreboard"] !== undefined ? globalData["scoreboard"] : {};
        var count = previousValues !== undefined && !isNaN(previousValues["wolfCount"]) ? previousValues["wolfCount"] + 1 : 1;
        if(count == 5) {
            return {
                "metaData": {
                    "completed":true
                }
            };
        }
        scoreboard["testPlayer"] = 100;
        scoreboard[contestantName] = count;
        previousValues["wolfCount"] = count;
        globalData["scoreboard"] = scoreboard;
        previousValues["globalData"] = globalData;
    }
    return previousValues;
}

function randomSmeltTest(previousValues, globalData, contestantName, uuid, playerName) {
    return {
        "metaData": {
            "completed": true
        }
    };
}

function interfaceBlockTest(previousValues, globalData, contestantName, fluid, fluidAmount, energy, item, itemAmount) {
    if(item === "minecraft:stone") {
        var count = !(previousValues === null) && !isNaN(previousValues.itemCount) ? previousValues.itemCount + itemAmount : itemAmount;
        if(count >= 5) {
            return {
                "metaData": {
                    "completed": true
                }
            };
        } else {
            return {
                "metaData": {
                    "completed": false
                },
                "itemCount": count
            };
        }
    }
    return {
        "metaData": {
            "completed": false
        },
        "itemCount": previousValues === null ? 0 : previousValues.itemCount
    };
}

function foodTimerExample(previousValues, globalData, contestantName, uuid, playerName, food, hungerRestore, saturation) {
    var scoreboard = globalData != null && globalData["scoreboard"] != null ? globalData["scoreboard"] : {};

	if(globalData != null && globalData["0_player_eat_food_timeUp"]) {
        var winningContestant;
        var winningCount = 0;
        if(globalData.contestantMap === undefined) {
			return {
                "globalData": {
                    "winners": []
                }
            };
        }
        else {
            Object.keys(globalData.contestantMap).forEach(function(key) {
                if(globalData.contestantMap[key] > winningCount) {
                    winningContestant = key;
                    winningCount = globalData.contestantMap[key];
                }
            });
            return {
                "globalData": {
                    "winners": [
                        winningContestant
                    ]
                }
            };
        }
    }
    if(saturation > 0) {
        if(globalData.contestantMap === undefined) {
            globalData.contestantMap = {};
            globalData.contestantMap[contestantName] = saturation;

			scoreboard[contestantName] = globalData.contestantMap[contestantName];
			globalData["scoreboard"] = scoreboard;
            return {
				"metaData": {
					"broadcastMessage": "test1 " + saturation
				},
                "globalData": globalData
            };
        }
        else {
            if(isNaN(globalData.contestantMap[contestantName])) {
                globalData.contestantMap[contestantName] = saturation;
            } else {
                globalData.contestantMap[contestantName] += saturation;
            }

			scoreboard[contestantName] = globalData.contestantMap[contestantName];
			globalData["scoreboard"] = scoreboard;
            return {
                "globalData": globalData
            }
        }
    }
}

function areaCheck(previousValues, globalData, contestantName, uuid, playerName, PosX, PosY, PosZ, biome, inVillage) {
    return {
        "metaData": {
            "completed": false,
            "broadcastMessage": "Our position: " + PosX + ", " + PosY + ", " + PosZ,
        },
        "globalData": globalData
    }
}

function koth(previousValues, globalData, contestantName, uuid, playerName, PosX, PosY, PosZ, biome, inVillage) {
    var scoreboard = globalData !== undefined && globalData["scoreboard"] !== undefined ? globalData["scoreboard"] : {};
	var playersOnHill = 0;
	var Positions = [
		globalData["startData"]["0"]["area_check"]["position"]["x"],
		globalData["startData"]["0"]["area_check"]["position"]["z"]
	]

	if(globalData !== null && globalData.time_up && previousValues !== null) {
        var winningContestant;
        var winningCount = 0;
        if(globalData.contestantMap === undefined) {
			globalData["winners"] = [];
			previousValues["globalData"] = globalData;
			previousValues["metaData"] = {
				"broadcastMessage": "ENDED",
				"completed": true
			}
            return previousValues
        } 
        else {
            Object.keys(globalData.contestantMap).forEach(function(key) {
                if(globalData.contestantMap[key] > winningCount) {
                    winningContestant = key;
                    winningCount = globalData.contestantMap[key];
                }
            });
			globalData["winners"] = winningContestant;
			previousValues["globalData"] = globalData;
			previousValues["metaData"] = {
				"broadcastMessage": "ENDED",
				"completed": true
			}
            return previousValues
        }
	}

	//Determine if player is in area or not and set default score
	if(globalData.contestantMap === undefined) {
		globalData.contestantMap = {};
		globalData.contestantMap[contestantName + "InArea"] = 0;
		globalData.contestantMap[contestantName] = 0;
	} else {
		if(isNaN(globalData.contestantMap[contestantName + "InArea"])) {
			globalData.contestantMap[contestantName + "InArea"] = 0;
			globalData.contestantMap[contestantName] = 0;
		} else {
			//if((Math.abs(Positions[0] - PosX) <= 50) && (Math.abs(Positions[1] - PosZ) <= 50)) {
			if(true) {
				globalData.contestantMap[contestantName + "InArea"] = 1;
				//previousValues["meta_data"] = {"broadcast_message": contestantName + " is on the hill"}
			} else {
				globalData.contestantMap[contestantName + "InArea"] = 0;
			}
		}
	}
	
	//Count number of players in area
	if(globalData !== null) {
		if(globalData.contestantMap !== undefined) {
			Object.keys(globalData.contestantMap).forEach(function(key) {
				if(key === contestantName + "InArea") {
					if(globalData.contestantMap[key] === 1) {
						//previousValues["meta_data"] = {"broadcast_message": "PLUSD 1"}
						playersOnHill += 1
					}	
				}
			});	
		}
		scoreboard["playersOnHill"] = playersOnHill;
	}
	
	//If one player is on the hill, give them score
	if(playersOnHill === 1) {
		globalData.contestantMap[contestantName] += 1;
	}
	
	scoreboard[contestantName] = globalData.contestantMap[contestantName];
	globalData["scoreboard"] = scoreboard;
	
	previousValues["globalData"] = globalData;
	return previousValues
}