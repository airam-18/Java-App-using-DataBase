SHOW DATABASES;

use navalwarsbd;

CREATE TABLE Clase(
    clasa VARCHAR(50) PRIMARY KEY,
    tip VARCHAR(2) CHECK (tip in('nr','cr')),
    tara VARCHAR(20),
    nr_arme iNT,
    diametru_tun DECIMAL(5,2),
    deplasament INT,
    propulsie VARCHAR(50)
);

CREATE TABLE Nave(
    nume VARCHAR(50) PRIMARY KEY,
    clasa VARCHAR(50),
    anul_lansarii INT,
    FOREIGN KEY(clasa) REFERENCES Clase(clasa)
);

CREATE TABLE Batalii(
    nume VARCHAR(50) PRIMARY KEY,
    data DATE,
    locatie VARCHAR(100)
);

CREATE TABLE Consecinte(
    nava    VARCHAR(50),
    batalie VARCHAR(50),
    rezultat VARCHAR(50) CHECK( rezultat IN ('scufundat','avariat','nevatamat')),
    PRIMARY KEY(nava,batalie),
    FOREIGN KEY(nava) REFERENCES Nave(nume),
    FOREIGN KEY(batalie) REFERENCES Batalii(nume)
);

INSERT INTO Clase VALUES('Dreadnought', 'nr', 'UK', 10, 12.0, 9000, 'aburi');
INSERT INTO Clase VALUES('Iowa', 'nr', 'USA', 9, 16.0, 10000, 'diesel');
INSERT INTO Clase VALUES('Baltimore', 'cr', 'USA', 12, 8.0, 14000, 'diesel');
INSERT INTO Clase VALUES('Sverdlov', 'cr', 'URSS', 12, 6.0, 13500, 'aburi');
INSERT INTO Clase VALUES('Fuso', 'nr', 'Japonia', 12, 14.0, 9800, 'aburi');
INSERT INTO Clase VALUES('Alaska','nr','USA',12,10.5,1423,'aburi');

INSERT INTO Nave VALUES('HMS Dreadnought', 'Dreadnought', 1906);
INSERT INTO Nave VALUES('USS Iowa', 'Iowa', 1943);
INSERT INTO Nave VALUES('USS Baltimore', 'Baltimore', 1943);
INSERT INTO Nave VALUES('Sverdlov', 'Sverdlov', 1903);
INSERT INTO Nave VALUES('Fuso', 'Fuso', 1914);
INSERT INTO Nave VALUES('Yamashiro', 'Fuso', 1915);

INSERT INTO Batalii VALUES('Battle of Chemulpo Bay', DATE '1904-02-09', 'Coreea');
INSERT INTO Batalii VALUES('Battle of Leyte Gulf', DATE '1944-10-23', 'Pacific');
INSERT INTO Batalii VALUES('Battle of Surigao Strait', DATE '1944-10-25', 'Filipine');
INSERT INTO Batalii VALUES('Battle of North Cape',DATE '1943-12-26', 'Atlantic');
INSERT INTO Batalii VALUES('Battle of Tsushima',DATE '1905-05-27', 'Japonia');

INSERT INTO Consecinte VALUES('HMS Dreadnought', 'Battle of North Cape', 'nevatamat');
INSERT INTO Consecinte VALUES('Sverdlov','Battle of Chemulpo Bay','nevatamat');
INSERT INTO Consecinte VALUES('USS Iowa', 'Battle of Leyte Gulf', 'nevatamat');
INSERT INTO Consecinte VALUES('USS Baltimore', 'Battle of Leyte Gulf', 'avariat');
INSERT INTO Consecinte VALUES('Fuso', 'Battle of Surigao Strait', 'scufundat');
INSERT INTO Consecinte VALUES('Yamashiro', 'Battle of Surigao Strait', 'scufundat');
INSERT INTO Consecinte VALUES('Sverdlov', 'Battle of Tsushima', 'nevatamat');
INSERT INTO Consecinte VALUES('HMS Dreadnought', 'Battle of Tsushima', 'avariat');

ALTER TABLE Clase
DROP COLUMN propulsie;



ALTER TABLE Nave
ADD CONSTRAINT chk_anul_lansarii
CHECK(anul_lansarii BETWEEN 1775 AND 1962);

ALTER TABLE Clase
ADD CONSTRAINT chk_tip
CHECK(tip<> 'nr' OR deplasament<=10000);

SELECT clasa,tara,deplasament
FROM Clase
WHERE tip='cr'
AND deplasament>10000
ORDER BY deplasament;

SELECT nume,anul_lansarii
FROM Nave
WHERE anul_lansarii BETWEEN 1859 AND 1946
ORDER BY anul_lansarii ASC,clasa DESC;

SELECT n.nume, c.deplasament,c.nr_arme,b.nume, b.data, b.locatie, con.rezultat
FROM Nave n
JOIN Clase c ON n.clasa = c.clasa
JOIN Consecinte con ON n.nume = con.nava
JOIN Batalii b ON con.batalie=b.nume
WHERE n.nume IN(
    SELECT n2.nume
    FROM Nave n2
    JOIN Consecinte con2 on n2.nume=con2.nava
    WHERE con2.batalie = 'Battle of Chemulpo Bay'
);

SELECT n1.nume AS nume1,
       n2.nume AS nume2,
       n1.anul_lansarii
FROM Nave n1
JOIN Nave n2 ON n1.anul_lansarii = n2.anul_lansarii
AND n1.clasa <> n2.clasa
AND n1.nume < n2.nume
ORDER BY n1.anul_lansarii, n1.nume,n2.nume;

SELECT *
FROM Batalii b
WHERE NOT EXISTS (
    SELECT 1
    FROM Batalii b2
    WHERE b2.data < b.data
);


SELECT *
FROM Clase
WHERE nr_arme IN(
    SELECT nr_arme
    FROM Clase
    Where clasa='Alaska'
);

SELECT tara, tip, Count(*) AS nr_clase, MIN(diametru_tun) as diametru_minim, AVG(diametru_tun) AS diametru_mediu, MAX(diametru_tun) AS diametru_maxim
FROM Clase
GROUP BY tara,tip
ORDER BY tara, tip;


SELECT batalie, COUNT(nava) AS nr_nave
FROM consecinte
GROUP BY batalie;

INSERT INTO Clase
VALUES('Nelson','cr','UK',9,16,18000);

INSERT INTO Nave
VALUES('Nelson','Nelson',1927);

INSERT INTO Nave
VALUES('Rodney','Nelson',1927);

DELETE
FROM Batalii b
WHERE NOT EXISTS(
    SELECT 1
    FROM Consecinte c
    WHERE c.batalie=b.nume
);

UPDATE Clase
SET diametru_tun=diametru_tun*2.54;


CREATE TABLE Exceptii(
    nava    VARCHAR(50),
    batalie VARCHAR(50),
    rezultat VARCHAR(50),
    natura VARCHAR(50),
    PRIMARY KEY (nava, batalie, rezultat)
);

INSERT INTO Exceptii (nava, batalie, rezultat, natura) VALUES
('Fuso', 'Battle of Leyte Gulf', 'avariat', 'exceptie: scufundat inainte');

INSERT INTO Exceptii (nava, batalie, rezultat, natura) VALUES
('Yamashiro', 'Battle of Leyte Gulf', 'nevatamat', 'exceptie: scufundat inainte');

INSERT INTO Exceptii (nava, batalie, rezultat, natura) VALUES
('HMS Dreadnought', 'Battle of Chemulpo Bay', 'nevatamat', 'exceptie: scufundat inainte');


-- Șterge procedura dacă există
DROP PROCEDURE IF EXISTS DetecteazaExceptii;

DELIMITER //

CREATE PROCEDURE DetecteazaExceptii()
BEGIN
    INSERT INTO Exceptii(nava, batalie, rezultat, natura)
    SELECT c2.nava, c2.batalie, c2.rezultat, 'exceptie: scufundat inainte'
    FROM Consecinte c2
    JOIN Batalii b2 ON c2.batalie = b2.nume
    WHERE c2.rezultat IN ('avariat','nevatamat')
      AND EXISTS (
          SELECT 1
          FROM Consecinte c1
          JOIN Batalii b1 ON c1.batalie = b1.nume
          WHERE c1.nava = c2.nava
            AND c1.rezultat = 'scufundat'
            AND b1.data < b2.data
      );
END //

DELIMITER ;

-- Șterge trigger-ul dacă există
DROP TRIGGER IF EXISTS trg_adjust_deplasament;

DELIMITER //

CREATE TRIGGER trg_adjust_deplasament
BEFORE INSERT ON Clase
FOR EACH ROW
BEGIN
    IF NEW.tip = 'cr' AND NEW.deplasament < 7000 THEN
        SET NEW.deplasament = 7000;
    END IF;
END //

DELIMITER ;

DROP TRIGGER IF EXISTS trg_adjust_deplasament_update;

DELIMITER //

CREATE TRIGGER trg_adjust_deplasament_update
BEFORE UPDATE ON Clase
FOR EACH ROW
BEGIN
    IF NEW.tip = 'cr' AND NEW.deplasament < 7000 THEN
        SET NEW.deplasament = 7000;
    END IF;
END //

DELIMITER ;


CREATE VIEW NaveItalia AS
SELECT clasa, nume, tip, nr_arme, diametru_tun, deplasament, anul_lansarii,
batalie, rezultat
FROM Clase NATURAL JOIN  Nave
JOIN  Consecinte ON Nave.nume = Consecinte.nava
WHERE tara = 'Italia';

-- Procedură pentru inserarea unei nave în view-ul NaveItalia
DROP PROCEDURE IF EXISTS InsertNaveItalia;

DELIMITER //

CREATE PROCEDURE InsertNaveItalia(
    IN p_nume VARCHAR(50),
    IN p_clasa VARCHAR(50),
    IN p_anul_lansarii INT
)
BEGIN
    INSERT INTO Nave(nume, clasa, anul_lansarii)
    VALUES (p_nume, p_clasa, p_anul_lansarii);
END //

DELIMITER ;

