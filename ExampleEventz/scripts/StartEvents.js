function randomizer(eventHistory, avaliableEvents) {
    var random1 = Math.random();
    /** 
     * Random event generator with no bias.
     */
    // return avaliableEvents[Math.floor(random1 * avaliableEvents.length)];

    /** 
     * Random event generator with bias
     *  Has a 50% chance to pick koth. Else, it creates a new random number and picks anything. 
     */
    // if(random1 < .5) {
    //     return "koth";
    // } else {
    //     return avaliableEvents[Math.floor(Math.random() * avaliableEvents.length)];
    // }

    /**
     * Random event generator based on a pool
     */
    var pool = [];
    // Create a function to reduce written code
    var addToPool = function(event, count) {
        for(var i = 0; i < count; i++) {
            pool.push(event);
        }
    }

    addToPool("koth", 5);
    addToPool("eating_contest", 2);
    addToPool("test_event", 1);
    return pool[Math.floor(random1 * pool.length)];
}

function startRecipeHunt() {
    var ingredientList = [
        "minecraft:dark_oak_log",
        "minecraft:sand",
        "minecraft:oak_planks"
    ];
    return {
        "potentialIngredients": [ingredientList[Math.floor(Math.random() * ingredientList.length)]]
    };
}

function startInterfaceBlockTest() {
    return {
        "itemInput": "minecraft:stone"
    };
}

function startFoodExample() {
    return {
        "timer": Date.now() + (1000 * 10)
    };
}

function kingOfTheHill() {
    return {
        "broadcastMessage": "Boundary from (0,0) to (20, 20)",
        "visibleBoundary": {
            "xMin": 0,
            "xMax": 20,
            "zMin": 0,
            "zMax": 20
        },
        "timer": Date.now() + (1000 * 10)
    };
}

function testEventStartMethod() {
    return {
        "random": Math.random()
    };
}

function testBroadcastMsg() {
    return {
        "broadcastMessage": "Test test",
        "mob": "minecraft:wolf",
        "runCommand": "tp @a 0 5 0"
    };
}

function kothStart() {
    var position = [
		Math.floor(Math.random() * 100),
		Math.floor(Math.random() * 100)
	]
	
	return {
        "timer": Date.now() + Date.now() + (1000 * 60) * 10,
		"broadcastMessage": "Hill Location: " + position[0] + ", " + position[1],
		"position": {
			"x": position[0],
			"z": position[1]
		},
        "visibleBoundary": {
            "xMin": position[0] - 50,
            "xMax": position[0] + 50,
            "zMin": position[1] - 50,
            "zMax": position[1] + 50
        }
    }
}