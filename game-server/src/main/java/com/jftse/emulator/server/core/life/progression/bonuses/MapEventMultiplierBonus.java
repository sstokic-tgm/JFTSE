package com.jftse.emulator.server.core.life.progression.bonuses;

import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusDecorator;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.battle.SGuardianMultiplier;
import com.jftse.server.core.jdbc.JdbcUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.function.Function;

// Not currently used by anything (tower-mode tried it and reverted - see the comment on
// TowerMode#buildBonusChain in the tower-mode plugin for why - a random-guardian mode isn't
// naturally tied to "this particular map" the way the classic guardian match is). Left in place,
// not deleted, since it's a generic, reusable decorator for any future caller that grants exp/gold
// directly instead of through the classic pot and wants the same map-level event applied.
//
// The same "Multiplier to Map" mechanism (S_Relationships.relationship_id=6, role_id 2=Exp
// multiplier/3=Gold multiplier, pointing at an S_Guardian_Multiplier row for the actual factor)
// admins use for map-wide "Nx exp/gold" events in the classic guardian mode - see
// MatchplayGuardianGame#getMatchRewards, which applies it once to the whole match's accumulated
// exp/gold pot. Guardian- and boss-guardian-level multipliers (relationship_id 4/5, keyed to a
// specific Guardian2Maps row) aren't handled here - only meaningful for callers that pick
// guardians via Guardian2Maps in the first place.
//
// Multiple active rows for the same map+role sum together (matching getMatchRewards' own
// behavior) rather than multiplying - in practice only one is ever active per map+role, since
// enabling a new event means flipping the old row's status instead of stacking a second one.
public class MapEventMultiplierBonus extends ExpGoldBonusDecorator {
    private static final long ROLE_EXP = 2L;
    private static final long ROLE_GOLD = 3L;

    // Resolved once per match (the map doesn't change mid-match) and reused via the other
    // constructor - see resolveMultipliers. A caller that instantiates this decorator once per
    // grant (e.g. once per floor) should resolve once up front and pass the cached result in,
    // rather than letting the Long-mapId constructor re-query the DB every time.
    public record Multipliers(double exp, double gold) {
    }

    private final double expMultiplier;
    private final double goldMultiplier;

    public MapEventMultiplierBonus(ExpGoldBonus expGoldBonus, Long mapId) {
        this(expGoldBonus, resolveMultipliers(mapId));
    }

    public MapEventMultiplierBonus(ExpGoldBonus expGoldBonus, Multipliers multipliers) {
        super(expGoldBonus);
        this.expMultiplier = multipliers.exp();
        this.goldMultiplier = multipliers.gold();
    }

    public static Multipliers resolveMultipliers(Long mapId) {
        JdbcUtil jdbcUtil = ServiceManager.getInstance().getJdbcUtil();
        // Block lambdas (explicit return) rather than expression lambdas here - JdbcUtil.execute
        // is overloaded with both a void Operation and a value-returning Function, and an
        // expression lambda satisfies both (ambiguous); a block lambda with a return statement
        // only satisfies the Function overload.
        double exp = jdbcUtil.execute((Function<EntityManager, Double>) em -> {
            return sumActiveMultiplier(em, mapId, ROLE_EXP);
        });
        double gold = jdbcUtil.execute((Function<EntityManager, Double>) em -> {
            return sumActiveMultiplier(em, mapId, ROLE_GOLD);
        });
        return new Multipliers(exp, gold);
    }

    @Override
    public int calculateExp() {
        return (int) Math.round(super.calculateExp() * expMultiplier);
    }

    @Override
    public int calculateGold() {
        return (int) Math.round(super.calculateGold() * goldMultiplier);
    }

    private static double sumActiveMultiplier(EntityManager em, Long mapId, long roleId) {
        TypedQuery<SGuardianMultiplier> query = em.createQuery("SELECT sgm FROM SRelationships sr " +
                "LEFT JOIN FETCH SGuardianMultiplier sgm ON sgm.id = sr.id_f " +
                "WHERE sr.id_t = :mapId AND sr.status.id = 1 AND sr.relationship.id = 6 AND sr.role.id = :roleId", SGuardianMultiplier.class);
        query.setParameter("mapId", mapId);
        query.setParameter("roleId", roleId);
        List<SGuardianMultiplier> multipliers = query.getResultList();
        return multipliers.isEmpty() ? 1.0 : multipliers.stream().mapToDouble(SGuardianMultiplier::getMultiplier).sum();
    }
}
