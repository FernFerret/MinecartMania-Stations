package com.afforess.minecartmaniastation;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.event.MinecartEvent;
import com.afforess.minecartmaniacore.event.MinecartLaunchedEvent;
import com.afforess.minecartmaniacore.utils.DirectionUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;
import com.afforess.minecartmaniacore.utils.WordUtils;

public class SignCommands {

	public static void processStation(MinecartEvent event) {
		MinecartManiaMinecart minecart = event.getMinecart();
		
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			convertCraftBookSorter(sign);
			for (int k = 0; k < 4; k++) {
				//Setup initial data
				String str = sign.getLine(k);
				String newLine = str;
				String val[] = str.split(":");
				if (val.length != 2) {
					continue;
				}
				//Strip header and ending characters
				val[0] = StringUtils.removeBrackets(val[0]);
				val[1] = StringUtils.removeBrackets(val[1]);
				//Strip whitespace
				val[0] = val[0].trim();
				val[1] = val[1].trim();
				boolean valid = false;
				//end of data setup
				
				//empty minecart condition
				if (!valid) {
					valid = minecart.isStandardMinecart() && minecart.minecart.getPassenger() == null && str.toLowerCase().contains("empty");
				}
				
				//generic player condition
				if (!valid) {
					valid = minecart.hasPlayerPassenger() && str.toLowerCase().contains("player");
				}
				
				//mob (monster and animal) condition
				if (!valid) {
					valid = minecart.minecart.getPassenger() != null && !minecart.hasPlayerPassenger() && str.toLowerCase().contains("mob");
				}
				
				//Player station command processing
				if (!valid) {
					if (minecart.hasPlayerPassenger()) {
						valid = processStationCommand(minecart, str);
					}
				}
				
				//Player name matches sign name condition
				if (!valid) {
					valid = minecart.hasPlayerPassenger() && str.equalsIgnoreCase(minecart.getPlayerPassenger().getName());
				}
				
				//Player passenger contains item in hand condition
				if (!valid) {
					if (minecart.hasPlayerPassenger() && minecart.getPlayerPassenger().getItemInHand() != null) {
						Item itemInHand = Item.materialToItem(minecart.getPlayerPassenger().getItemInHand().getType());
						Item[] signData = ItemUtils.getItemStringToMaterial(val[0].trim());
						for (Item item : signData) {
							if (item != null && item.equals(itemInHand)) {
								valid = true;
								break;
							}
						}		
					}
				}
				
				//Storage minecart contains item(s) condition
				if (!valid) {
					if (minecart.isStorageMinecart()) {
						Item[] signData = ItemUtils.getItemStringToMaterial(val[0].trim());
						for (Item item : signData) {
							if (item != null && (((MinecartManiaStorageCart)minecart).contains(item))) {
								valid = true;
								break;
							}
						}	
					}
				}
				
				//empty storage minecart condition
				if (!valid) {
					valid = minecart.isStorageMinecart() && str.toLowerCase().contains("cargo") && ((MinecartManiaStorageCart)minecart).isEmpty();
				}
				
				//Storage minecart condition
				if (!valid) {
					valid = minecart.isStorageMinecart() && str.toLowerCase().contains("storage");
				}
				
				//Powered minecart condition
				if (!valid) {
					valid = minecart.isPoweredMinecart() && str.toLowerCase().contains("powered");
				}
				
				//Redstone power condition
				if (!valid) {
					valid = str.toLowerCase().contains("redstone") && (minecart.isPoweredBeneath() ||
							MinecartManiaWorld.isBlockIndirectlyPowered(minecart.minecart.getWorld(), minecart.getX(), minecart.getY() - 2, minecart.getZ()));
				}

				
				//Note getDirectionOfMotion is unreliable on curves, use getPreviousFacingDir instead.
				//Direction condition handling
				if (!valid && (val[0].equals("W") || val[0].toLowerCase().contains("west"))) {
					valid = minecart.getPreviousFacingDir() == DirectionUtils.CompassDirection.WEST;
				}
				else if (!valid && (val[0].equals("E") || val[0].toLowerCase().contains("east"))) {
					valid = minecart.getPreviousFacingDir() == DirectionUtils.CompassDirection.EAST;
				}
				else if (!valid && (val[0].equals("N") || val[0].toLowerCase().contains("north"))) {
					valid = minecart.getPreviousFacingDir() == DirectionUtils.CompassDirection.NORTH;
				}
				else if (!valid && (val[0].equals("S") || val[0].toLowerCase().contains("south"))) {
					valid = minecart.getPreviousFacingDir() == DirectionUtils.CompassDirection.SOUTH;
				}
				
				if (valid) {
					CompassDirection direction = CompassDirection.NO_DIRECTION;
					
					//Process STR first because of overlapping characters
					if (val[1].equals("STR") || val[1].toLowerCase().contains("straight")) {
						direction = minecart.getPreviousFacingDir();
					}
					else if (val[1].equals("W") || val[1].toLowerCase().contains("west")) {
						direction = DirectionUtils.CompassDirection.WEST;
					}
					else if (val[1].equals("E") || val[1].toLowerCase().contains("east")) {
						direction = DirectionUtils.CompassDirection.EAST;
					}
					else if (val[1].equals("S") || val[1].toLowerCase().contains("south")) {
						direction = DirectionUtils.CompassDirection.SOUTH;
					}
					else if (val[1].equals("N") || val[1].toLowerCase().contains("north")) {
						direction = DirectionUtils.CompassDirection.NORTH;
					}
					else if (val[1].equals("L") || val[1].toLowerCase().contains("left")) {
						direction = DirectionUtils.getLeftDirection(minecart.getPreviousFacingDir());
					}
					else if (val[1].equals("R") || val[1].toLowerCase().contains("right")) {
						direction = DirectionUtils.getRightDirection(minecart.getPreviousFacingDir());
					}
					else if (val[1].equals("D") || val[1].toLowerCase().contains("destroy")) {
						direction = null;
					}
					
					//Special case - if we are at a launcher, set the launch speed as well
					if (event instanceof MinecartLaunchedEvent && direction != null && direction != CompassDirection.NO_DIRECTION) {
						minecart.setMotion(direction, 0.6D);
						((MinecartLaunchedEvent)event).setLaunchSpeed(minecart.minecart.getVelocity());
					}
					
					//setup sign formatting
					newLine = StringUtils.removeBrackets(newLine);
					char[] ch = {' ', ':'};
					newLine = WordUtils.capitalize(newLine, ch);
					newLine = StringUtils.addBrackets(newLine);
					
					boolean handled = false;
					//Handle minecart destruction
					if (direction == null) {
						minecart.kill();
						handled = true;
					}
					else if (MinecartUtils.validMinecartTrack(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ(), 2, direction)) {
						int data = DirectionUtils.getMinetrackRailDataForDirection(direction, minecart.getPreviousFacingDir());
						if (data != -1) {
							handled = true;
							
							//Force the game to remember the old data of the rail we are on, and reset it once we are done
							Block oldBlock = MinecartManiaWorld.getBlockAt(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ());
							ArrayList<Integer> blockData = new ArrayList<Integer>();
							blockData.add(new Integer(oldBlock.getX()));
							blockData.add(new Integer(oldBlock.getY()));
							blockData.add(new Integer(oldBlock.getZ()));
							blockData.add(new Integer(oldBlock.getData()));
							minecart.setDataValue("old rail data", blockData);
							
							//change the track dirtion
							MinecartManiaWorld.setBlockData(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ(), data);
						}
						else if (DirectionUtils.getOppositeDirection(direction).equals(minecart.getPreviousFacingDir())) {
							//format the sign
							minecart.reverse();
							handled = true;
						}
					}
					
					if (handled){
						event.setActionTaken(true);
						//format the sign
						sign.setLine(k, newLine);
						sign.update(true);
						return;
					}
				}
			}
		}
	}

	private static boolean processStationCommand(MinecartManiaMinecart minecart, String str) {
		boolean valid = false;
		if (!str.toLowerCase().contains("st-")) {
			return false;
		}
		String[] val = str.toLowerCase().split(":");
		String[] keys = val[0].split("-| ?: ?");
		String st = keys[1];
		String stp = st; //st pattern
		String station = MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger()).getLastStation().toLowerCase();
		int parseSetting = MinecartManiaWorld.getIntValue(MinecartManiaWorld.getConfigurationValue("Station Sign Parsing Method"));
		switch(parseSetting){
			case 0: //default with no pattern matching
				valid = station.equalsIgnoreCase(st);break;
			case 1: //simple pattern matching
				stp = stp.replace("\\", "\\\\") //escapes backslashes in case people use them in station names
				.replace(".", "\\.") //escapes period
				.replace("*", ".*") //converts *
				.replace("?", ".") //converts ?
				.replace("#", "\\d") //converts #
				.replace("@", "[a-zA-Z]"); //converts @
			case 2: //full regex //note the lack of break before this, case 1 comes down here after converting
				valid = station.matches(stp); break;
		}
		if (valid && MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger()).getDataValue("Reset Station Data") == null) {
			MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger()).setLastStation("");
		}
		return valid;
	}

	private static void convertCraftBookSorter(Sign sign) {
		if (sign.getLine(1).contains("[Sort]")) {
			if (!sign.getLine(2).trim().isEmpty()) {
				sign.setLine(2, "st-" + sign.getLine(2).trim().substring(1) + ": L");
			}
			if (!sign.getLine(3).trim().isEmpty()) {
				sign.setLine(3, "st-" + sign.getLine(3).trim().substring(1) + ": R");
			}
			sign.setLine(1, "");
			sign.update();
		}
	}

	public static ArrayList<CompassDirection> getRestrictedDirections(MinecartManiaMinecart minecart) {
		ArrayList<CompassDirection> restricted = new ArrayList<CompassDirection>(4);
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				if (sign.getLine(i).toLowerCase().contains("restrict")) {
					String[] directions = sign.getLine(i).split(":");
					if (directions.length > 1) {
						for (int j = 1; j < directions.length; j++) {
							if (directions[j].contains("N")) {
								restricted.add(CompassDirection.NORTH);
							}
							if (directions[j].contains("S")) {
								restricted.add(CompassDirection.SOUTH);
							}
							if (directions[j].contains("E")) {
								restricted.add(CompassDirection.EAST);
							}
							if (directions[j].contains("W")) {
								restricted.add(CompassDirection.WEST);
							}
						}
						return restricted;
					}
				}
			}
		}
		return restricted;
	}
}
