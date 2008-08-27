package games.stendhal.server.maps.quests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.kalavan.citygardens.IceCreamSellerNPC;
import games.stendhal.server.maps.kalavan.citygardens.LittleGirlNPC;
import games.stendhal.server.maps.kalavan.citygardens.MummyNPC;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class IcecreamForAnnieTest {


	private static String questSlot = "icecream_for_annie";
	
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();
		
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		
		new IceCreamSellerNPC().configureZone(zone, null);
		new LittleGirlNPC().configureZone(zone, null);
		new MummyNPC().configureZone(zone, null);
			
		final AbstractQuest quest = new IcecreamForAnnie();
		quest.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		final double oldkarma = player.getKarma();
		
		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Hello, my name is Annie. I am five years old.", npc.get("text"));
		en.step(player, "help");
		assertEquals("Ask my mummy.", npc.get("text"));
		en.step(player, "job");
		assertEquals("I help my mummy.", npc.get("text"));
		en.step(player, "offer");
		assertEquals("I'm a little girl, I haven't anything to offer.", npc.get("text"));
		en.step(player, "task");
		assertEquals("I'm hungry! I'd like an icecream, please. Vanilla, with a chocolate flake. Will you get me one?", npc.get("text"));
		en.step(player, "ok");
		assertEquals("Thank you!", npc.get("text"));
		assertThat(player.getKarma(), greaterThan(oldkarma));
		assertThat(player.getQuest(questSlot), is("start"));
		en.step(player, "bye");
		assertEquals("Ta ta.", npc.get("text"));
		
		en.step(player, "hi");
		assertEquals("Hello. I'm hungry.", npc.get("text"));
		en.step(player, "task");
		assertEquals("Waaaaaaaa! Where is my icecream ....", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Ta ta.", npc.get("text"));
		
		npc = SingletonRepository.getNPCList().get("Sam");
		en = npc.getEngine();
		
		Item item = ItemTestHelper.createItem("money", 30);
		player.getSlot("bag").add(item);
		
		en.step(player, "hi");
		assertEquals("Hi. Can I #offer you an icecream?", npc.get("text"));
		en.step(player, "yes");
		en.step(player, "offer");
		assertEquals("I sell icecream.", npc.get("text"));
		en.step(player, "buy icecream");
		assertEquals("1 icecream will cost 30. Do you want to buy it?", npc.get("text"));
		en.step(player, "yes");
		assertEquals("Congratulations! Here is your icecream!", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye, enjoy your day!", npc.get("text"));
		assertTrue(player.isEquipped("icecream"));
		
		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Mummy says I mustn't talk to you any more. You're a stranger.", npc.get("text"));
		
		npc = SingletonRepository.getNPCList().get("Mrs Jones");
		en = npc.getEngine();
		
		en.step(player, "hi");
		assertEquals("Hello, I see you've met my daughter Annie. I hope she wasn't too demanding. You seem like a nice person.", npc.get("text"));
		assertThat(player.getQuest(questSlot), is("mummy"));
		en.step(player, "task");
		assertEquals("Nothing, thank you.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Bye for now.", npc.get("text"));
		
		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();
		
		final int xp = player.getXP();
		final double karma = player.getKarma();
		en.step(player, "hi");
		assertEquals("Yummy! Is that icecream for me?", npc.get("text"));
		en.step(player, "yes");
		// [15:06] kymara earns 500 experience points. 
		assertFalse(player.isEquipped("icecream"));
		assertTrue(player.isEquipped("present"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));
		assertTrue(player.getQuest(questSlot).startsWith("eating"));
		assertEquals("Thank you EVER so much! You are very kind. Here, take this present.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Ta ta.", npc.get("text"));
		
		en.step(player, "hi");
		assertEquals("Hello.", npc.get("text"));
		en.step(player, "task");
		assertEquals("I've had too much icecream. I feel sick.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Ta ta.", npc.get("text"));

		// -----------------------------------------------


		// -----------------------------------------------
		final double newKarma = player.getKarma();
		// [15:07] Changed the state of quest 'icecream_for_annie' from 'eating;1219676807283' to 'eating;0' 
		player.setQuest(questSlot, "eating;0");
		en.step(player, "hi");
		assertEquals("Hello.", npc.get("text"));
		en.step(player, "task");
		assertEquals("I hope another icecream wouldn't be greedy. Can you get me one?", npc.get("text"));
		en.step(player, "no");
		assertThat(player.getQuest(questSlot), is("rejected"));
		assertThat(player.getKarma(), lessThan(newKarma));
		assertEquals("Ok, I'll ask my mummy instead.", npc.get("text"));

		// -----------------------------------------------
		
		en.step(player, "hi");
		assertEquals("Hello.", npc.get("text"));
		en.step(player, "bye");
		assertEquals("Ta ta.", npc.get("text"));
	}
}