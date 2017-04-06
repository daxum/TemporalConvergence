package daxum.temporalconvergence;

import java.util.Random;

//Note from the future: wtf?
//This is vital to the proper operation of the mod
public class InitLogBuilder {
	private static final Random rand = new Random();

	public static String get() {
		String toReturn = "What, were you expecting something?";
		int i = rand.nextInt(); //Why is there no nextShort()?
		/*unsigned (why java? D:)*/ int temp = i & 127; //would be a byte, but operators cast to int anyway

		if (temp < PREFIXES.length)
			toReturn = PREFIXES[temp];
		else
			toReturn = PREFIXES[temp % PREFIXES.length];

		char end = toReturn.charAt(toReturn.length() - 1);

		if (end == '.' || end == '!' || end == ':')
			toReturn += " T";
		else
			toReturn += " t";

		temp = i >> 7 & 127; //Shift one less than the next byte because not using the eighth bit on the previous

		String adj = temp < ADJECTIVES.length ? ADJECTIVES[temp] : ADJECTIVES[temp % ADJECTIVES.length];
		char first = adj.toLowerCase().charAt(0);

		if (first == 'a' || first == 'e' || first == 'i' || first == 'o' || first == 'u') //This might need work
			toReturn += "here's an ";
		else
			toReturn += "here's a ";

		toReturn += adj;

		temp = i >> 14 & 127;

		if (temp < NOUNS.length)
			toReturn += " " + NOUNS[temp];
		else
			toReturn += " " + NOUNS[temp % NOUNS.length];

		toReturn += " behind you with ";

		temp = i >> 21 & 127;

		String sn = temp < NOUNS2.length ? NOUNS2[temp] : NOUNS2[temp % NOUNS2.length];
		char snd = sn.toLowerCase().charAt(0);

		if (snd == 'a' || snd == 'e' || snd == 'i' || snd == 'o' || snd == 'u')
			toReturn += "an ";
		else
			toReturn += "a ";

		toReturn += sn + "!";

		if ((System.currentTimeMillis() & 42) == 42) { //1/8 chance
			temp = i >> 28 & 15; //Last four bits
		toReturn += " " + (temp < SUFFIXES.length ? SUFFIXES[temp] : SUFFIXES[temp % SUFFIXES.length]);
		}

		return toReturn; //This was an hour well spent.
	}

	private static final String[] PREFIXES = {"Look out!", "Warning:", "Watch out!", "Um...", "Ack!", "Hey, listen!", "Just so you know,", "Don't panic, but", "Hey!", "Don't move!", "Don't look now, but"};
	private static final String[] ADJECTIVES = {"angry", "shady", "unhappy", "depressed", "joyful", "annoyed", "upside down", "inverted", "explosive", "electric", "random", "vengeful", "insane", "fat", "hungry"};
	private static final String[] NOUNS = {"ferret", "elephant", "alien", "person", "ghost", "computer", "robot", "moose", "tree", "Nazg" + (char) 251 + "l", "wizard", "dragon", "chicken", "ostrich", "penguin"};
	private static final String[] NOUNS2 = {"knife", "spatula", "chemistry textbook"/*The horror!*/, "chainsaw", "lawyer", "toaster", "sharp object", "spork", "flamethrower", "spoon", "fork", "tank"};
	//Max length of 15
	private static final String[] SUFFIXES = {"Just thought you should know.", "You might want to run.", "Maybe do something about that?", "I'm sure it's fine.", "Stay calm!", "(Help, I'm trapped in a witty comment generator!)"};
}
