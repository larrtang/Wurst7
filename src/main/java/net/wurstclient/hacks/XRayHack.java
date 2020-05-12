/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.screens.EditBlockListScreen;
import net.wurstclient.events.GetAmbientOcclusionLightLevelListener;
import net.wurstclient.events.RenderBlockEntityListener;
import net.wurstclient.events.SetOpaqueCubeListener;
import net.wurstclient.events.ShouldDrawSideListener;
import net.wurstclient.events.TesselateBlockListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.util.BlockUtils;

@SearchTags({"XRay", "x ray", "OreFinder", "ore finder"})
public final class XRayHack extends Hack implements UpdateListener,
	SetOpaqueCubeListener, GetAmbientOcclusionLightLevelListener,
	ShouldDrawSideListener, TesselateBlockListener, RenderBlockEntityListener
{
	private final BlockListSetting ores = new BlockListSetting("Ores", "",
		"minecraft:anvil", "minecraft:beacon", "minecraft:bone_block",
		"minecraft:bookshelf", "minecraft:brewing_stand",
		"minecraft:chain_command_block", "minecraft:chest", "minecraft:clay",
		"minecraft:coal_block", "minecraft:coal_ore", "minecraft:command_block",
		"minecraft:crafting_table", "minecraft:diamond_block",
		"minecraft:diamond_ore", "minecraft:dispenser", "minecraft:dropper",
		"minecraft:emerald_block", "minecraft:emerald_ore",
		"minecraft:enchanting_table", "minecraft:end_portal",
		"minecraft:end_portal_frame", "minecraft:ender_chest",
		"minecraft:furnace", "minecraft:glowstone", "minecraft:gold_block",
		"minecraft:gold_ore", "minecraft:hopper", "minecraft:iron_block",
		"minecraft:iron_ore", "minecraft:ladder", "minecraft:lapis_block",
		"minecraft:lapis_ore", "minecraft:lava", "minecraft:mossy_cobblestone",
		"minecraft:nether_portal", "minecraft:nether_quartz_ore",
		"minecraft:redstone_block", "minecraft:redstone_ore",
		"minecraft:repeating_command_block", "minecraft:spawner",
		"minecraft:tnt", "minecraft:torch", "minecraft:trapped_chest");


	public class TimeoutThread extends Thread {
		@Override
		public void run() {
			//doneRender = false;
			renderTicks = 0;
			while(!doneRender && !stopThread) {
				try {
					renderTicks++;
					Thread.sleep(1000);
					if (renderTicks > TIMEOUT) doneRender = true;
				} catch (Exception e) {
				}
			}
		}
	}
	private ArrayList<String> oreNames;
	private static long TIMEOUT = 10;
	private volatile long renderTicks = 0;
	private volatile boolean doneRender = true;
	private TimeoutThread timeoutThread;
	private volatile boolean stopThread = false;

	public XRayHack()
	{
		super("X-Ray", "Allows you to see ores through walls.");
		setCategory(Category.RENDER);
		addSetting(ores);
	}
	
	@Override
	public String getRenderName()
	{
		return "X-Wurst";
	}
	
	@Override
	public void onEnable()
	{
		//timeoutThread = new TimeoutThread();
		//doneRender = false;
		renderTicks = 0;
		//timeoutThread.start();


		oreNames = new ArrayList<>(new BlockListSetting("Ores", "",
				"minecraft:chest",
				"minecraft:diamond_ore").getBlockNames());
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(SetOpaqueCubeListener.class, this);
		EVENTS.add(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.add(ShouldDrawSideListener.class, this);
		EVENTS.add(TesselateBlockListener.class, this);
		EVENTS.add(RenderBlockEntityListener.class, this);
		MC.worldRenderer.reload();

		ClientPlayerEntity player = MC.player;
		if (player != null) {
			// Vec3d playerPos = player.getPos();
			BlockPos initPlayerPos = player.getBlockPos();
			BlockPos pos;
			int radius = 4;
			for (int i = -radius; i < radius; i++) {
				for (int j = -radius; j < radius; j++) {
					for (int k = -radius; k < radius; k++) {
						pos = initPlayerPos.add(i, j ,k);
						player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN));
					}
				}
			}
		}
		else {
			System.err.println("MC.player NPE!");
		}

		MC.worldRenderer.reload();

	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(SetOpaqueCubeListener.class, this);
		EVENTS.remove(GetAmbientOcclusionLightLevelListener.class, this);
		EVENTS.remove(ShouldDrawSideListener.class, this);
		EVENTS.remove(TesselateBlockListener.class, this);
		EVENTS.remove(RenderBlockEntityListener.class, this);
		MC.worldRenderer.reload();
		
		if(!WURST.getHax().fullbrightHack.isEnabled())
			MC.options.gamma = 0.5F;
	}
	
	@Override
	public void onUpdate()
	{
		MC.options.gamma = 16;
	}
	
	@Override
	public void onSetOpaqueCube(SetOpaqueCubeEvent event)
	{
		event.cancel();
	}
	
	@Override
	public void onGetAmbientOcclusionLightLevel(
		GetAmbientOcclusionLightLevelEvent event)
	{
		event.setLightLevel(1);
	}
	
	@Override
	public void onShouldDrawSide(ShouldDrawSideEvent event)
	{
		event.setRendered(isVisible(event.getState().getBlock()));
	}
	
	@Override
	public void onTesselateBlock(TesselateBlockEvent event)
	{
		if(!isVisible(event.getState().getBlock()))
			event.cancel();
	}
	
	@Override
	public void onRenderBlockEntity(RenderBlockEntityEvent event)
	{
		if(!isVisible(BlockUtils.getBlock(event.getBlockEntity().getPos()))) {
			event.cancel();
		}
	}
	
	public void openBlockListEditor(Screen prevScreen)
	{
		MC.openScreen(new EditBlockListScreen(prevScreen, ores));
	}
	
	private boolean isVisible(Block block)
	{
		String name = BlockUtils.getName(block);
		int index = Collections.binarySearch(oreNames, name);
		return index >= 0;
	}
}
