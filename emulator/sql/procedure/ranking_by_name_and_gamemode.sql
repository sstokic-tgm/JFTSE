DELIMITER //

DROP PROCEDURE IF EXISTS ranking_by_name_and_gamemode //
CREATE PROCEDURE ranking_by_name_and_gamemode(IN name varchar(255), IN gameMode tinyint)
BEGIN
	SET @ranking := 0;

    DROP TEMPORARY TABLE IF EXISTS tempRanking;
	CREATE TEMPORARY TABLE tempRanking AS (
		SELECT
			p.name,
			ps.basicRP,
			ps.battleRP,
			ps.guardianRP
		FROM
			Player p
			LEFT JOIN PlayerStatistic ps ON ps.id = p.playerStatistic_id
		WHERE
		    p.alreadyCreated = 1
		ORDER BY
			CASE
				WHEN gameMode = 0 THEN ps.basicRP
				WHEN gameMode = 1 THEN ps.battleRP
				WHEN gameMode = 2 THEN ps.guardianRP
			END DESC,
			p.created
		);

	SELECT
	    tr.ranking
	FROM
		(
			SELECT
				*,
				@ranking := @ranking + 1 AS ranking
			FROM tempRanking
		) tr
	WHERE
		tr.name COLLATE utf8_bin = name;

END //

DELIMITER ;