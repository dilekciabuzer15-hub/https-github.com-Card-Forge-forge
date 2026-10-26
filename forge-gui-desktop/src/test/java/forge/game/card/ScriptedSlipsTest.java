package forge.game.card;

import org.testng.annotations.Test;

import forge.ai.AITest;
import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.staticability.StaticAbilityCantAttackBlock;
import forge.game.zone.ZoneType;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/** Card scripts whose written form did not do what the card's Oracle text says. */
public class ScriptedSlipsTest extends AITest {

    /** "You lose 1 life for each card put into your hand this way": two Islands and two others, any pick. */
    @Test(timeOut = 60000)
    public void strokeOfLuckLosesOneLifePerCardPutIntoHand() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        p.getZone(ZoneType.Library).removeAllCards(true);
        addCardToZone("Island", p, ZoneType.Library);
        addCardToZone("Island", p, ZoneType.Library);
        addCardToZone("Forest", p, ZoneType.Library);
        addCardToZone("Swamp", p, ZoneType.Library);
        int handBefore = p.getCardsIn(ZoneType.Hand).size();
        int lifeBefore = p.getLife();
        Card spell = addCardToZone("Stroke of Luck", p, ZoneType.Hand);
        SpellAbility sa = spell.getSpells().get(0);
        sa.setActivatingPlayer(p);

        AbilityUtils.resolve(sa);

        int taken = p.getCardsIn(ZoneType.Hand).size() - handBefore - 1; // minus the spell itself
        assertTrue("at least one card was taken", taken >= 1);
        assertEquals("one life per card taken", taken, lifeBefore - p.getLife());
    }

    /** "It can't be blocked this turn": after the ability resolves no creature can block the Trespasser. */
    @Test(timeOut = 60000)
    public void trespasserCantBeBlockedAfterItsAbilityResolves() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Player opp = game.getPlayers().get(0);
        Card trespasser = addCard("_____ _____ _____ Trespasser", p);
        Card blocker = addCard("Grizzly Bears", opp);
        SpellAbility sa = trespasser.getSpellAbilities().get(0);
        sa.setActivatingPlayer(p);

        AbilityUtils.resolve(sa);
        game.getAction().checkStateEffects(true);

        assertTrue("nothing blocks it", StaticAbilityCantAttackBlock.cantBlockBy(trespasser, blocker));
    }

    /** The creature half of the redirect: the Effect it creates must know which creature was targeted. */
    @Test(timeOut = 60000)
    public void captainsManeuverChoosesTheTargetedCreature() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card bears = addCard("Grizzly Bears", game.getPlayers().get(0));
        Card spell = addCardToZone("Captain's Maneuver", p, ZoneType.Hand);
        SpellAbility sa = spell.getSpells().get(0);
        sa.setActivatingPlayer(p);
        sa.getTargets().add(bears);

        AbilityUtils.resolve(sa);

        Card effect = null;
        for (Card c : game.getCardsIn(ZoneType.Command)) {
            if (c.getName().contains("Captain's Maneuver")) {
                effect = c;
            }
        }
        assertTrue("the effect exists", effect != null);
        assertTrue("it redirects damage to the targeted creature", effect.getChosenCards().contains(bears));
    }
}
