/***************************************************************************
 *                    Copyright © 2020-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.COUNT_CRUSHER;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.COUNT_DICTATOR;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.COUNT_DOMINATOR;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.COUNT_LACKEY;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.COUNT_SOLDIER;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.ID_CRUSHER;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.ID_DICTATOR;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.ID_DOMINATOR;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.ID_LACKEY;
import static games.stendhal.server.core.rp.achievement.factory.KillBlordroughsAchievementFactory.ID_SOLDIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.ados.barracks.BuyerNPC;
import games.stendhal.server.maps.quests.KillBlordroughs;
import utilities.AchievementTestHelper;
import utilities.PlayerTestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class KillBlordroughsAchievementFactoryTest extends ZonePlayerAndNPCTestImpl {

	private static final Logger logger = Logger.getLogger(KillBlordroughsAchievementFactoryTest.class);

	private Player player;
	private SpeakerNPC npc;
	private static final KillBlordroughs questInstance = KillBlordroughs.getInstance();
	private static final String QUEST_SLOT = questInstance.getSlotName();
	private static final StendhalQuestSystem questSystem = StendhalQuestSystem.get();

	private final String enemy = "blordrough corporal";

	private static final String idPrefix = "quest.special.kill_blordroughs.";
	private static final Map<String, Integer> idList = new LinkedHashMap<String, Integer>() {{
		put(idPrefix + "0005", 5);
		put(idPrefix + "0025", 25);
		put(idPrefix + "0050", 50);
		put(idPrefix + "0100", 100);
		put(idPrefix + "0250", 250);
	}};


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AchievementTestHelper.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		PlayerTestHelper.removeAllPlayers();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		zone = setupZone("testzone");
		addZoneConfigurator(new BuyerNPC(), "testzone");
		setNpcNames("Mrotho");

		super.setUp();

		npc = getNPC("Mrotho");
		questSystem.loadQuest(questInstance);
		AchievementTestHelper.setEnemyNames(enemy);
	}

	@Test
	public void init() {
		resetPlayer();
		testNPC();
		testAchievementsActive();
		testAchievements();

		// double-check that we ran through all achievements
		for (final String id: idList.keySet()) {
			assertTrue(achievementReached(id));
		}
	}

	private void testNPC() {
		assertNotNull(npc);
		assertEquals("Mrotho", npc.getName());
	}

	private void testAchievementsActive() {
		for (final String id: idList.keySet()) {
			final boolean active = AchievementTestHelper.achievementEnabled(id);
			logger.debug("achievement " + id + " active: " + String.valueOf(active));
			assertTrue(active);
		}
	}

	private void testAchievements() {
		assertNull(player.getQuest(QUEST_SLOT));

		for (int idx = 0; idx < COUNT_CRUSHER; idx++) {
			completeQuest();
			assertEquals(idx+1, questInstance.getCompletedCount(player));

			if (idx >= COUNT_LACKEY - 1) {
				assertTrue(achievementReached(ID_LACKEY));
			} else {
				assertFalse(achievementReached(ID_LACKEY));
			}

			if (idx >= COUNT_SOLDIER - 1) {
				assertTrue(achievementReached(ID_SOLDIER));
			} else {
				assertFalse(achievementReached(ID_SOLDIER));
			}

			if (idx >= COUNT_DOMINATOR - 1) {
				assertTrue(achievementReached(ID_DOMINATOR));
			} else {
				assertFalse(achievementReached(ID_DOMINATOR));
			}

			if (idx >= COUNT_DICTATOR - 1) {
				assertTrue(achievementReached(ID_DICTATOR));
			} else {
				assertFalse(achievementReached(ID_DICTATOR));
			}

			if (idx >= COUNT_CRUSHER - 1) {
				assertTrue(achievementReached(ID_CRUSHER));
			} else {
				assertFalse(achievementReached(ID_CRUSHER));
			}
		}
	}


	/**
	 * Resets player achievements.
	 */
	private void resetPlayer() {
		player = null;
		assertNull(player);
		player = PlayerTestHelper.createPlayer("player");
		assertNotNull(player);

		assertNull(player.getQuest(QUEST_SLOT));

		AchievementTestHelper.init(player);
		for (final String id: idList.keySet()) {
			assertFalse(achievementReached(id));
		}
	}

	/**
	 * Checks if the player has reached the achievement.
	 *
	 * @return
	 * 		<code>true</player> if the player has the achievement.
	 */
	private boolean achievementReached(final String ID) {
		return AchievementTestHelper.achievementReached(player, ID);
	}

	/**
	 * Completes quest one time.
	 */
	private void completeQuest() {
		final Engine en = npc.getEngine();

		final int completedCount = questInstance.getCompletedCount(player);

		assertFalse(questIsActive());

		// reset kill count & set quest state to repeatable
		player.setSoloKillCount(enemy, 0);
		if (player.getQuest(QUEST_SLOT) != null) {
			player.setQuest(QUEST_SLOT, 1, "0");
		}

		en.step(player, "hi");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		en.step(player, "quest");
		assertTrue(questIsActive());
		en.step(player, "bye");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());

		player.setSoloKillCount(enemy, 100);

		en.step(player, "hi");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		en.step(player, "done");
		assertFalse(questIsActive());
		assertEquals(completedCount + 1, questInstance.getCompletedCount(player));
		en.step(player, "bye");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
	}

	/**
	 * Checks if the quest is active.
	 */
	private boolean questIsActive() {
		return new QuestActiveCondition(QUEST_SLOT).fire(player, null, npc);
	}
}
