package daxum.temporalconvergence.power;

import java.util.Random;

import daxum.temporalconvergence.TemporalConvergence;
import daxum.temporalconvergence.world.SaveDataHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

//WARNING: Wall of comments incoming!
public class PowerDimension implements INBTSerializable<NBTTagCompound> {
	//These four values don't need to be saved.
	public static final int MAX_INSTABILITY = 300; //Determines how long a dimension can stay above maxIoRate before it destabilizes.
	public final int id; //The unique id for the dimension. Use this to obtain the proper dimension using get below.
	private final SaveDataHandler sdh; //This needs to be here so the dimension can delete itself / mark save data as dirty.
	private final Random rand = new Random();

	//All values below this are used for update logic
	private int amount = 0; //The current amount stored.
	private int instability = 0; //The current instability - increases if ioRate is above maxIoRate, causes destabilization if it's above maxStability.
	private int ioRate = 0; //The current ioRate. Changes based on power demands, increases drastically if unstable.
	private int powerDrawn = 0; //The amount of power drawn this tick. Gets reset every tick, used to increase i/o rate.
	private int powerInserted = 0; //Same as above, but for input.
	private int powerRequested = 0; //The power requested per tick.
	private int attemptedInsertion = 0; //The amount attempted to insert.
	private int unstableTicks = 0; //How long the dimension has been unstable.
	private int lossFrequency = 1; //Approximately how often a dimension looses power when stable. Cannot be negative, set to zero to disable all passive loss.

	//These would be final, but they're set by deserializeNBT.
	private int lossRate = 0; //The base time loss rate. Actual time loss rate is determined by amount stored and stability.
	private int maxAmount = 0; //Max amount the dimension is capable of storing before loss rate begins increasing exponentially.
	private int maxIoRate = 0; //The max ioRate before stability starts decreasing.

	private boolean stable = true; //Whether the dimension is stable.
	private int frozenCount; //The number of objects keeping this dimension frozen

	//Use makeNew or get to obtain an instance.
	//Don't use SaveDataHandler to get a new instance, as it will not be initialized.
	public PowerDimension(SaveDataHandler s, int ID) {
		id = ID;
		sdh = s;
	}

	public void update() {
		if (!isActive() || amount <= 0) return;

		//Update i/o

		//If i/o rate needs increasing
		if (powerDrawn >= ioRate * 0.9 || powerInserted >= ioRate * 0.9) {
			if (stable) {
				if (ioRate < maxIoRate * 1.25 && !(ioRate == maxIoRate && powerRequested <= maxIoRate && attemptedInsertion <= maxIoRate)) //Don't go over maxIoRate unless necessary
					ioRate++;
			}
			else {
				ioRate += Math.max(powerInserted, powerDrawn) / 10 + 1;
			}
		}
		//If i/o rate needs decreasing
		else if (powerDrawn <= ioRate * 0.7 || powerInserted <= ioRate * 0.7) {
			if (ioRate > 0)
				ioRate--;
		}

		//Update amount based on power loss

		if (stable && lossFrequency > 0 && rand.nextInt(lossFrequency) == 0) {
			if (amount <= maxAmount)
				amount -= lossRate;
			else
				amount -= lossRate + (amount - maxAmount) * 0.1 + 1;
		}
		else if (!stable) {
			amount -= (int) Math.pow(lossRate, 0.02 * unstableTicks + 1);
		}

		if (amount <= 0) { //Dimension out of time, collapses
			sdh.removePowerDim(id);
			return;
		}

		//Update stability

		if (ioRate <= maxIoRate && amount <= maxAmount && instability > 0)
			instability--;
		else if (instability <= MAX_INSTABILITY){
			if (ioRate > maxIoRate)
				instability += 3;
			if (amount > maxAmount)
				instability++;
		}

		//Update stable

		if (stable && instability > MAX_INSTABILITY)
			stable = false;
		if (!stable && instability <= 0) {
			stable = true;
			unstableTicks = 0;
		}

		//Finish up

		if (!stable) {
			if (ioRate > maxIoRate)
				unstableTicks++;
			else
				unstableTicks--; //A bit less harsh on the whole "exponential" thing
		}

		powerDrawn = 0;
		powerInserted = 0;
		powerRequested = 0;
		attemptedInsertion = 0;

		sdh.markDirty();
	}

	//Returns the amount of power gotten
	public int getPower(int amountRequested) {
		powerRequested += amountRequested;

		if (!isActive() || powerDrawn >= ioRate || amount <= 0)
			return 0;

		int returnAmount = 0;

		if (powerDrawn + amountRequested >= ioRate) {
			returnAmount = ioRate - powerDrawn;
		}
		else {
			returnAmount = amountRequested;
		}

		if (returnAmount >= amount) {
			returnAmount = amount;
			amount = 0;
		}
		else {
			amount -= returnAmount;
		}

		powerDrawn += returnAmount;
		sdh.markDirty();
		return returnAmount;
	}

	//Returns the uninserted power
	public int insertPower(int insertAmount) {
		attemptedInsertion += insertAmount;

		if (!isActive() || powerInserted >= ioRate)
			return insertAmount;
		if (amount <= 0)
			return 0; //nom nom nom (This is here in case someone caches the dimension, normally this wouldn't happen)

		int unInserted = insertAmount;

		if (powerInserted + insertAmount >= ioRate) {
			unInserted = insertAmount - (ioRate - powerInserted);
		}
		else {
			unInserted = 0;
		}

		amount += insertAmount - unInserted;
		powerInserted += insertAmount - unInserted;
		sdh.markDirty();
		return unInserted;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound comp = new NBTTagCompound();

		comp.setBoolean("stable", stable);
		comp.setInteger("freezers", frozenCount);

		int[] values = {amount, instability, ioRate, lossRate, maxAmount, maxIoRate, unstableTicks, lossFrequency};

		comp.setIntArray("storagedata", values);

		return comp;
	}

	@Override
	public void deserializeNBT(NBTTagCompound comp) {
		if (comp.hasKey("stable"))
			stable = comp.getBoolean("stable");
		if (comp.hasKey("freezers"))
			frozenCount = comp.getInteger("freezers");
		if (comp.hasKey("storagedata")) {
			int[] values = comp.getIntArray("storagedata");

			if (values.length >= 8) {
				amount = values[0];
				instability = values[1];
				ioRate = values[2];
				lossRate = values[3];
				maxAmount = values[4];
				maxIoRate = values[5];
				unstableTicks = values[6];
				lossFrequency = values[7];
			}
			else {
				TemporalConvergence.LOGGER.error("Could not load values for power dimension #" + id + ". Only " + values.length + " values found. 8 were expected.");
			}
		}
	}

	//I can't remember why I made this private. Why did I make this private...?
	private void init(int initialAmount, int timeLossRate, int maxSafeAmount, int maxSafeIoRate, int timeLossFrequency) {
		amount = initialAmount;
		lossRate = timeLossRate;
		maxAmount = maxSafeAmount;
		maxIoRate = maxSafeIoRate;

		if (timeLossFrequency >= 0)
			lossFrequency = timeLossFrequency;
	}

	public boolean isActive() {
		return frozenCount == 0;
	}

	public double getPowerRatio() {
		return (double)amount / maxAmount;
	}

	public void addFreezer() {
		frozenCount++;
		sdh.markDirty();
	}

	public void removeFreezer() {
		if (frozenCount > 0) {
			frozenCount--;
			sdh.markDirty();
		}
	}

	public static PowerDimension makeNew(World world, int initialAmount, int timeLossRate, int maxSafeAmount, int maxSafeIoRate, int timeLossFrequency) {
		PowerDimension newDim = SaveDataHandler.get(world).getNewPowerDim(); //This marks the SaveDataHandler as dirty, so I don't need to repeat it here.
		newDim.init(initialAmount, timeLossRate, maxSafeAmount, maxSafeIoRate, timeLossFrequency);

		return newDim;
	}

	public static PowerDimension get(World world, int id) {
		return SaveDataHandler.get(world).getExistingPowerDim(id);
	}


	public static void updateDimensions(World world) {
		SaveDataHandler current = SaveDataHandler.get(world);

		PowerDimension next = null;
		for (int i = 0; i < current.getMaxDimId(); i++) {
			next = current.getExistingPowerDim(i);

			if (next != null) {
				next.update();
			}
		}
	}

	@Override
	public String toString() {
		String out = "";

		out += "Dimension Id: " + id + "\n";
		out += "Active: " + isActive() + "\n";
		out += "Number holding inactive: " + frozenCount + "\n";
		out += "Amount Stored: " + amount + "\n";
		out += "Max Amount Stored: " + maxAmount + "\n";
		out += "I/O Rate: " + ioRate + "\n";
		out += "Max I/O Rate: " + maxIoRate + "\n";
		out += "Stable: " + stable + "\n";
		out += "Instability: " + instability + "\n";
		out += "Power Loss Rate: " + lossRate + "\n";
		out += "Power Loss Frequency: " + lossFrequency + " (Every x ticks)\n";

		return out;
	}
}
