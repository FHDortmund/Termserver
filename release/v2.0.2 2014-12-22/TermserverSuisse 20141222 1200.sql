-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.5.37-0ubuntu0.14.04.1


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema c1TermServer
--

CREATE DATABASE IF NOT EXISTS c1TermServer;
USE c1TermServer;

--
-- Definition of table `association_type`
--

DROP TABLE IF EXISTS `association_type`;
CREATE TABLE `association_type` (
  `codeSystemEntityVersionId` bigint(20) NOT NULL,
  `forwardName` varchar(50) NOT NULL,
  `reverseName` varchar(50) NOT NULL,
  PRIMARY KEY (`codeSystemEntityVersionId`),
  KEY `codeSystemEntityVersionId` (`codeSystemEntityVersionId`),
  CONSTRAINT `FK_asstype_code_system_entity_version` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_entity_version` (`versionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `association_type`
--

/*!40000 ALTER TABLE `association_type` DISABLE KEYS */;
INSERT INTO `association_type` (`codeSystemEntityVersionId`,`forwardName`,`reverseName`) VALUES 
 (4,'ist Unterklasse von','ist Oberklasse von');
/*!40000 ALTER TABLE `association_type` ENABLE KEYS */;


--
-- Definition of table `code_system`
--

DROP TABLE IF EXISTS `code_system`;
CREATE TABLE `code_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currentVersionId` bigint(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  `insertTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `codeSystemType` varchar(30) DEFAULT NULL,
  `descriptionEng` text,
  `website` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system`
--

/*!40000 ALTER TABLE `code_system` DISABLE KEYS */;
INSERT INTO `code_system` (`id`,`currentVersionId`,`name`,`description`,`insertTimestamp`,`codeSystemType`,`descriptionEng`,`website`) VALUES 
 (1,1,'Community Portal Index (CPI)',NULL,'2014-11-17 08:46:34',NULL,NULL,NULL),
 (2,4,'Rollen Index (RI)',NULL,'2014-11-20 09:28:44',NULL,NULL,NULL),
 (4,5,'healthcareFacilityTypeCode','Typ der Gesundheitseinrichtung','2014-11-20 15:39:41',NULL,NULL,NULL),
 (5,6,'authorSpecialty','Medizinische Fachrichtung der Person','2014-11-20 15:42:07',NULL,NULL,NULL),
 (6,7,'practiceSettingCode',NULL,'2014-11-24 11:48:50',NULL,NULL,NULL),
 (7,8,'Vertraulichkeitsstufen',NULL,'2014-11-24 14:58:04',NULL,NULL,NULL);
/*!40000 ALTER TABLE `code_system` ENABLE KEYS */;


--
-- Definition of table `code_system_concept`
--

DROP TABLE IF EXISTS `code_system_concept`;
CREATE TABLE `code_system_concept` (
  `codeSystemEntityVersionId` bigint(20) NOT NULL,
  `code` varchar(100) NOT NULL,
  `term` text NOT NULL,
  `termAbbrevation` varchar(50) DEFAULT NULL,
  `description` text,
  `isPreferred` tinyint(1) DEFAULT NULL,
  `meaning` text,
  `hints` text,
  PRIMARY KEY (`codeSystemEntityVersionId`),
  KEY `codeSystemEntityVersionId` (`codeSystemEntityVersionId`),
  CONSTRAINT `FK_csc_code_system_entity_version` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_entity_version` (`versionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_concept`
--

/*!40000 ALTER TABLE `code_system_concept` DISABLE KEYS */;
INSERT INTO `code_system_concept` (`codeSystemEntityVersionId`,`code`,`term`,`termAbbrevation`,`description`,`isPreferred`,`meaning`,`hints`) VALUES 
 (5,'1','e-toile',NULL,NULL,1,NULL,NULL),
 (6,'2','Rete sanitaria',NULL,NULL,1,NULL,NULL),
 (7,'3','eHealth VD',NULL,NULL,1,NULL,NULL),
 (10,'1','administrativ',NULL,'Ermöglicht ausschliesslich den Zugriff auf die Daten der Vertraulichkeitsstufe „Demografische Daten\"',1,NULL,NULL),
 (11,'2','eingeschränkt',NULL,'Ermöglicht den Zugriff auf die Vertraulichkeitsstufen „Demografische Daten“ und „Nützliche Daten“',1,NULL,NULL),
 (12,'3','normal',NULL,'Ermöglicht den Zugriff auf die Vertraulichkeitsstufen „Demografische Daten“, „Nützliche Daten“ sowie „Medizinische Daten“',1,NULL,NULL),
 (13,'4','erweitert',NULL,'Ermöglicht den Zugriff auf die Vertraulichkeitsstufen „Demografische Daten“, „Nützliche Daten“, „Medizinische Daten“ sowie „Sensible Daten“',1,NULL,NULL),
 (14,'5','Notfall',NULL,'Ermöglicht im Falle eines medizinischen Notfalls auch ohne vorgängig erteiltes Zugriffsrecht durch den Patienten den Zugriff auf die Vertraulichkeitsstufen „Demografische Daten“, „Nützliche Daten“ sowie „Medizinische Daten“. Der Patient ist über die erfolgten Zugriffe nachträglich zu informieren. Der Patient kann den Zugriff in medizinischen Notfallsituationen jederzeit untersagen oder auf die Vertraulichkeitsstufen „Demografische Daten“ und „Nützliche Daten“ einschränken.',1,NULL,NULL),
 (15,'6','gesamt',NULL,'Diese Zugriffsstufe ist dem Patienten vorbehalten. Somit kann der Patient alle Daten aller Vertraulichkeitsstufen, insbesondere auch die Vertraulichkeitsstufe „Geheim Daten“, einsehen. Es ist nicht vorgesehen, dass diese Zugriffsstufe über die Rechtematrix verändert werden kann.',1,NULL,NULL),
 (16,'50001','Fachärztin/Facharzt für Allgemeine Innere Medizin',NULL,NULL,1,NULL,NULL),
 (17,'190001','Institut für medizinische Diagnostik',NULL,NULL,1,NULL,NULL),
 (18,'190002','Notfalleinrichtung oder Organisation des Rettungswesens',NULL,NULL,1,NULL,NULL),
 (19,'190003','Gesundheitsbehörde',NULL,NULL,1,NULL,NULL),
 (20,'190004','Organisation für Pflege zu Hause',NULL,NULL,1,NULL,NULL),
 (21,'190005','Spital',NULL,NULL,1,NULL,NULL),
 (22,'190006','Psychiatrie Spital',NULL,NULL,1,NULL,NULL),
 (23,'190007','Gesundheitseinrichtung in der Haftanstalt',NULL,NULL,1,NULL,NULL),
 (24,'190008','Organisation für stationäre Krankenpflege',NULL,NULL,1,NULL,NULL),
 (25,'190009','Apotheke',NULL,NULL,1,NULL,NULL),
 (26,'190010','Hausarztpraxis',NULL,NULL,1,NULL,NULL),
 (27,'190011','Facharztpraxis',NULL,NULL,1,NULL,NULL),
 (28,'190012','Organisation für stationäre Rehabilitation',NULL,NULL,1,NULL,NULL),
 (29,'190013','Kur- und Präventions-Einrichtung',NULL,NULL,1,NULL,NULL),
 (30,'190999','Andere Gesundheits-Organisation',NULL,NULL,1,NULL,NULL),
 (31,'50002','Fachärztin/Facharzt für Anästhesiologie',NULL,NULL,1,NULL,NULL),
 (32,'50003','Fachärztin/Facharzt für Arbeitsmedizin',NULL,NULL,1,NULL,NULL),
 (33,'50004','Fachärztin/Facharzt für Chirurgie',NULL,NULL,1,NULL,NULL),
 (34,'50005','Fachärztin/Facharzt für Dermatologie und Venerologie',NULL,NULL,1,NULL,NULL),
 (35,'50006','Fachärztin/Facharzt für Endokrinologie / Diabetologie',NULL,NULL,1,NULL,NULL),
 (36,'50007','Fachärztin/Facharzt für Gastroenterologie',NULL,NULL,1,NULL,NULL),
 (37,'50008','Fachärztin/Facharzt für Gynäkologie und Geburtshilfe',NULL,NULL,1,NULL,NULL),
 (38,'50009','Fachärztin/Facharzt für Hämatologie',NULL,NULL,1,NULL,NULL),
 (39,'50010','Fachärztin/Facharzt für Herz- und thorakale Gefässchirurgie',NULL,NULL,1,NULL,NULL),
 (40,'50011','Fachärztin/Facharzt für Infektiologie',NULL,NULL,1,NULL,NULL),
 (41,'50012','Fachärztin/Facharzt für Kardiologie',NULL,NULL,1,NULL,NULL),
 (42,'50013','Fachärztin/Facharzt für Kinder- und Jugendmedizin',NULL,NULL,1,NULL,NULL),
 (43,'50014','Fachärztin/Facharzt für Kinder- und Jugendpsychiatrie und -psychotherapie',NULL,NULL,1,NULL,NULL),
 (44,'50015','Fachärztin/Facharzt für Kinderchirurgie',NULL,NULL,1,NULL,NULL),
 (45,'50016','Fachärztin/Facharzt für Klinische Pharmakologie und Toxikologie',NULL,NULL,1,NULL,NULL),
 (46,'50017','Fachärztin/Facharzt für Mund-, Kiefer- und Gesichtschirurgie',NULL,NULL,1,NULL,NULL),
 (47,'50018','Fachärztin/Facharzt für Nephrologie',NULL,NULL,1,NULL,NULL),
 (48,'50019','Fachärztin/Facharzt für Neurochirurgie',NULL,NULL,1,NULL,NULL),
 (49,'50020','Fachärztin/Facharzt für Neurologie',NULL,NULL,1,NULL,NULL),
 (50,'50021','Fachärztin/Facharzt für Nuklearmedizin',NULL,NULL,1,NULL,NULL),
 (51,'50022','Fachärztin/Facharzt für Ophthalmologie',NULL,NULL,1,NULL,NULL),
 (52,'50023','Fachärztin/Facharzt für Orthopädische Chirurgie und Traumatologie des Bewegungsapparates',NULL,NULL,1,NULL,NULL),
 (53,'50024','Fachärztin/Facharzt für Oto-Rhino-Laryngologie',NULL,NULL,1,NULL,NULL),
 (54,'50025','Fachärztin/Facharzt für Pathologie',NULL,NULL,1,NULL,NULL),
 (55,'50026','Fachärztin/Facharzt für Physikalische Medizin und Rehabiliation',NULL,NULL,1,NULL,NULL),
 (56,'260001','Allergologie und klinische Immunologie',NULL,NULL,1,NULL,NULL),
 (57,'260002','Allgemeinmedizin',NULL,NULL,1,NULL,NULL),
 (58,'260003','Anästhesiologie',NULL,NULL,1,NULL,NULL),
 (59,'260004','Angiologie',NULL,NULL,1,NULL,NULL),
 (60,'260005','Pharmakologie',NULL,NULL,1,NULL,NULL),
 (61,'260006','Arbeitsmedizin',NULL,NULL,1,NULL,NULL),
 (62,'260007','Augenoptik',NULL,NULL,1,NULL,NULL),
 (63,'260008','Chiropraktik',NULL,NULL,1,NULL,NULL),
 (64,'260009','Chirurgie',NULL,NULL,1,NULL,NULL),
 (65,'260010','Dermatologie und Venerologie',NULL,NULL,1,NULL,NULL),
 (66,'260011','Endokrinologie/Diabetologie',NULL,NULL,1,NULL,NULL),
 (67,'260012','Ergotherapie',NULL,NULL,1,NULL,NULL),
 (68,'260013','Ernährungsberatung',NULL,NULL,1,NULL,NULL),
 (69,'260014','Geriatrie',NULL,NULL,1,NULL,NULL),
 (70,'260015','Gastroenterologie',NULL,NULL,1,NULL,NULL),
 (71,'260016','Gynäkologie und Geburtshilfe',NULL,NULL,1,NULL,NULL),
 (72,'260068','Hämatologie',NULL,NULL,1,NULL,NULL),
 (73,'260017','Handchirurgie',NULL,NULL,1,NULL,NULL),
 (74,'260018','Hebamme',NULL,NULL,1,NULL,NULL),
 (75,'260019','Herz- und thorakale Gefässchirurgie',NULL,NULL,1,NULL,NULL),
 (76,'260020','Infektiologie',NULL,NULL,1,NULL,NULL),
 (77,'260021','Innere Medizin',NULL,NULL,1,NULL,NULL),
 (78,'260022','Intensivmedizin',NULL,NULL,1,NULL,NULL),
 (79,'1','Demografische Daten',NULL,NULL,1,NULL,NULL),
 (80,'2','Nützliche Daten',NULL,NULL,1,NULL,NULL),
 (81,'3','Medizinische Daten',NULL,NULL,1,NULL,NULL),
 (82,'4','Sensible Daten',NULL,NULL,1,NULL,NULL),
 (83,'5','Geheime Daten',NULL,NULL,1,NULL,NULL),
 (84,'4','Infomed',NULL,NULL,1,NULL,NULL),
 (85,'5','Ponte Vecchio',NULL,NULL,1,NULL,NULL);
/*!40000 ALTER TABLE `code_system_concept` ENABLE KEYS */;


--
-- Definition of table `code_system_concept_translation`
--

DROP TABLE IF EXISTS `code_system_concept_translation`;
CREATE TABLE `code_system_concept_translation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeSystemEntityVersionId` bigint(20) NOT NULL,
  `term` text NOT NULL,
  `termAbbrevation` varchar(50) DEFAULT NULL,
  `languageCd` varchar(30) NOT NULL,
  `description` text,
  `meaning` text,
  `hints` text,
  PRIMARY KEY (`id`),
  KEY `codeSystemEntityVersionId` (`codeSystemEntityVersionId`),
  CONSTRAINT `FK_term_translation_term` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_concept` (`codeSystemEntityVersionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_concept_translation`
--

/*!40000 ALTER TABLE `code_system_concept_translation` DISABLE KEYS */;
/*!40000 ALTER TABLE `code_system_concept_translation` ENABLE KEYS */;


--
-- Definition of table `code_system_entity`
--

DROP TABLE IF EXISTS `code_system_entity`;
CREATE TABLE `code_system_entity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currentVersionId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_entity`
--

/*!40000 ALTER TABLE `code_system_entity` DISABLE KEYS */;
INSERT INTO `code_system_entity` (`id`,`currentVersionId`) VALUES 
 (4,4),
 (5,5),
 (6,6),
 (7,7),
 (10,10),
 (11,11),
 (12,12),
 (13,13),
 (14,14),
 (15,15),
 (16,16),
 (17,17),
 (18,18),
 (19,19),
 (20,20),
 (21,21),
 (22,22),
 (23,23),
 (24,24),
 (25,25),
 (26,26),
 (27,27),
 (28,28),
 (29,29),
 (30,30),
 (31,31),
 (32,32),
 (33,33),
 (34,34),
 (35,35),
 (36,36),
 (37,37),
 (38,38),
 (39,39),
 (40,40),
 (41,41),
 (42,42),
 (43,43),
 (44,44),
 (45,45),
 (46,46),
 (47,47),
 (48,48),
 (49,49),
 (50,50),
 (51,51),
 (52,52),
 (53,53),
 (54,54),
 (55,55),
 (56,56),
 (57,57),
 (58,58),
 (59,59),
 (60,60),
 (61,61),
 (62,62),
 (63,63),
 (64,64),
 (65,65),
 (66,66),
 (67,67),
 (68,68),
 (69,69),
 (70,70),
 (71,71),
 (72,72),
 (73,73),
 (74,74),
 (75,75),
 (76,76),
 (77,77),
 (78,78),
 (79,79),
 (80,80),
 (81,81),
 (82,82),
 (83,83),
 (84,84),
 (85,85);
/*!40000 ALTER TABLE `code_system_entity` ENABLE KEYS */;


--
-- Definition of table `code_system_entity_version`
--

DROP TABLE IF EXISTS `code_system_entity_version`;
CREATE TABLE `code_system_entity_version` (
  `versionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeSystemEntityId` bigint(20) NOT NULL,
  `previousVersionId` bigint(20) DEFAULT NULL,
  `insertTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `statusVisibility` int(11) NOT NULL,
  `statusVisibilityDate` datetime NOT NULL,
  `statusDeactivated` int(11) NOT NULL DEFAULT '0',
  `statusDeactivatedDate` datetime NOT NULL,
  `statusWorkflow` int(11) NOT NULL,
  `statusWorkflowDate` datetime NOT NULL,
  `effectiveDate` datetime DEFAULT NULL,
  `majorRevision` int(11) DEFAULT NULL,
  `minorRevision` int(11) DEFAULT NULL,
  `isLeaf` tinyint(3) unsigned DEFAULT '1',
  PRIMARY KEY (`versionId`),
  KEY `codeSystemEntityId` (`codeSystemEntityId`),
  KEY `codeSystemEntityId_2` (`codeSystemEntityId`),
  CONSTRAINT `FK_code_system_entity_version_code_system_entity` FOREIGN KEY (`codeSystemEntityId`) REFERENCES `code_system_entity` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_entity_version`
--

/*!40000 ALTER TABLE `code_system_entity_version` DISABLE KEYS */;
INSERT INTO `code_system_entity_version` (`versionId`,`codeSystemEntityId`,`previousVersionId`,`insertTimestamp`,`statusVisibility`,`statusVisibilityDate`,`statusDeactivated`,`statusDeactivatedDate`,`statusWorkflow`,`statusWorkflowDate`,`effectiveDate`,`majorRevision`,`minorRevision`,`isLeaf`) VALUES 
 (4,4,NULL,'2014-11-20 14:12:00',1,'2014-11-20 14:12:00',0,'2014-11-20 14:12:00',0,'2014-11-20 14:12:00','2014-11-20 14:12:00',NULL,NULL,1),
 (5,5,NULL,'2014-11-17 08:00:00',1,'2014-11-17 08:51:22',0,'2014-11-17 08:51:22',0,'2014-11-17 08:51:22','2014-11-17 08:51:22',NULL,NULL,1),
 (6,6,NULL,'2014-11-17 11:03:25',1,'2014-11-17 11:03:25',0,'2014-11-17 11:03:25',0,'2014-11-17 11:03:25','2014-11-17 11:03:25',NULL,NULL,1),
 (7,7,NULL,'2014-11-17 11:11:48',1,'2014-11-17 11:11:48',0,'2014-11-17 11:11:48',0,'2014-11-17 11:11:48','2014-11-17 11:11:48',NULL,NULL,1),
 (10,10,NULL,'2014-11-20 09:29:06',1,'2014-11-20 09:29:06',0,'2014-11-20 09:29:06',0,'2014-11-20 09:29:06','2014-11-20 09:29:06',NULL,NULL,1),
 (11,11,NULL,'2014-11-20 13:31:04',1,'2014-11-20 13:31:04',0,'2014-11-20 13:31:04',0,'2014-11-20 13:31:04','2014-11-20 13:31:04',NULL,NULL,1),
 (12,12,NULL,'2014-11-20 13:47:30',1,'2014-11-20 13:47:30',0,'2014-11-20 13:47:30',0,'2014-11-20 13:47:30','2014-11-20 13:47:30',NULL,NULL,1),
 (13,13,NULL,'2014-11-20 13:48:13',1,'2014-11-20 13:48:13',0,'2014-11-20 13:48:13',0,'2014-11-20 13:48:13','2014-11-20 13:48:13',NULL,NULL,1),
 (14,14,NULL,'2014-11-20 13:48:34',1,'2014-11-20 13:48:34',0,'2014-11-20 13:48:34',0,'2014-11-20 13:48:34','2014-11-20 13:48:34',NULL,NULL,1),
 (15,15,NULL,'2014-11-20 13:49:03',1,'2014-11-20 13:49:03',0,'2014-11-20 13:49:03',0,'2014-11-20 13:49:03','2014-11-20 13:49:03',NULL,NULL,1),
 (16,16,NULL,'2014-11-20 16:14:30',1,'2014-11-20 16:14:30',0,'2014-11-20 16:14:30',0,'2014-11-20 16:14:30','2014-11-20 16:14:30',NULL,NULL,1),
 (17,17,NULL,'2014-11-21 09:43:52',1,'2014-11-21 09:43:52',0,'2014-11-21 09:43:52',0,'2014-11-21 09:43:52','2014-11-21 09:43:52',NULL,NULL,1),
 (18,18,NULL,'2014-11-21 09:46:14',1,'2014-11-21 09:46:14',0,'2014-11-21 09:46:14',0,'2014-11-21 09:46:14','2014-11-21 09:46:14',NULL,NULL,1),
 (19,19,NULL,'2014-11-21 09:46:43',1,'2014-11-21 09:46:43',0,'2014-11-21 09:46:43',0,'2014-11-21 09:46:43','2014-11-21 09:46:43',NULL,NULL,1),
 (20,20,NULL,'2014-11-21 09:46:56',1,'2014-11-21 09:46:56',0,'2014-11-21 09:46:56',0,'2014-11-21 09:46:56','2014-11-21 09:46:56',NULL,NULL,1),
 (21,21,NULL,'2014-11-21 09:47:06',1,'2014-11-21 09:47:06',0,'2014-11-21 09:47:06',0,'2014-11-21 09:47:06','2014-11-21 09:47:06',NULL,NULL,1),
 (22,22,NULL,'2014-11-21 09:47:20',1,'2014-11-21 09:47:20',0,'2014-11-21 09:47:20',0,'2014-11-21 09:47:20','2014-11-21 09:47:20',NULL,NULL,1),
 (23,23,NULL,'2014-11-21 09:47:31',1,'2014-11-21 09:47:31',0,'2014-11-21 09:47:31',0,'2014-11-21 09:47:31','2014-11-21 09:47:31',NULL,NULL,1),
 (24,24,NULL,'2014-11-21 09:48:01',1,'2014-11-21 09:48:01',0,'2014-11-21 09:48:01',0,'2014-11-21 09:48:01','2014-11-21 09:48:01',NULL,NULL,1),
 (25,25,NULL,'2014-11-21 09:48:14',1,'2014-11-21 09:48:14',0,'2014-11-21 09:48:14',0,'2014-11-21 09:48:14','2014-11-21 09:48:14',NULL,NULL,1),
 (26,26,NULL,'2014-11-21 09:49:34',1,'2014-11-21 09:49:34',0,'2014-11-21 09:49:34',0,'2014-11-21 09:49:34','2014-11-21 09:49:34',NULL,NULL,1),
 (27,27,NULL,'2014-11-21 09:49:53',1,'2014-11-21 09:49:53',0,'2014-11-21 09:49:53',0,'2014-11-21 09:49:53','2014-11-21 09:49:53',NULL,NULL,1),
 (28,28,NULL,'2014-11-21 09:50:06',1,'2014-11-21 09:50:06',0,'2014-11-21 09:50:06',0,'2014-11-21 09:50:06','2014-11-21 09:50:06',NULL,NULL,1),
 (29,29,NULL,'2014-11-21 09:50:26',1,'2014-11-21 09:50:26',0,'2014-11-21 09:50:26',0,'2014-11-21 09:50:26','2014-11-21 09:50:26',NULL,NULL,1),
 (30,30,NULL,'2014-11-21 09:50:42',1,'2014-11-21 09:50:42',0,'2014-11-21 09:50:42',0,'2014-11-21 09:50:42','2014-11-21 09:50:42',NULL,NULL,1),
 (31,31,NULL,'2014-11-21 09:56:47',1,'2014-11-21 09:56:47',0,'2014-11-21 09:56:47',0,'2014-11-21 09:56:47','2014-11-21 09:56:47',NULL,NULL,1),
 (32,32,NULL,'2014-11-21 09:57:07',1,'2014-11-21 09:57:07',0,'2014-11-21 09:57:07',0,'2014-11-21 09:57:07','2014-11-21 09:57:07',NULL,NULL,1),
 (33,33,NULL,'2014-11-21 09:57:20',1,'2014-11-21 09:57:20',0,'2014-11-21 09:57:20',0,'2014-11-21 09:57:20','2014-11-21 09:57:20',NULL,NULL,1),
 (34,34,NULL,'2014-11-21 10:09:33',1,'2014-11-21 10:09:33',0,'2014-11-21 10:09:33',0,'2014-11-21 10:09:33','2014-11-21 10:09:33',NULL,NULL,1),
 (35,35,NULL,'2014-11-21 10:09:45',1,'2014-11-21 10:09:45',0,'2014-11-21 10:09:45',0,'2014-11-21 10:09:45','2014-11-21 10:09:45',NULL,NULL,1),
 (36,36,NULL,'2014-11-21 10:11:17',1,'2014-11-21 10:11:17',0,'2014-11-21 10:11:17',0,'2014-11-21 10:11:17','2014-11-21 10:11:17',NULL,NULL,1),
 (37,37,NULL,'2014-11-21 10:12:17',1,'2014-11-21 10:12:17',0,'2014-11-21 10:12:17',0,'2014-11-21 10:12:17','2014-11-21 10:12:17',NULL,NULL,1),
 (38,38,NULL,'2014-11-21 11:37:38',1,'2014-11-21 11:37:38',0,'2014-11-21 11:37:38',0,'2014-11-21 11:37:38','2014-11-21 11:37:38',NULL,NULL,1),
 (39,39,NULL,'2014-11-21 11:37:53',1,'2014-11-21 11:37:53',0,'2014-11-21 11:37:53',0,'2014-11-21 11:37:53','2014-11-21 11:37:53',NULL,NULL,1),
 (40,40,NULL,'2014-11-21 11:38:04',1,'2014-11-21 11:38:04',0,'2014-11-21 11:38:04',0,'2014-11-21 11:38:04','2014-11-21 11:38:04',NULL,NULL,1),
 (41,41,NULL,'2014-11-21 11:38:13',1,'2014-11-21 11:38:13',0,'2014-11-21 11:38:13',0,'2014-11-21 11:38:13','2014-11-21 11:38:13',NULL,NULL,1),
 (42,42,NULL,'2014-11-21 11:38:28',1,'2014-11-21 11:38:28',0,'2014-11-21 11:38:28',0,'2014-11-21 11:38:28','2014-11-21 11:38:28',NULL,NULL,1),
 (43,43,NULL,'2014-11-21 11:40:36',1,'2014-11-21 11:40:36',0,'2014-11-21 11:40:36',0,'2014-11-21 11:40:36','2014-11-21 11:40:36',NULL,NULL,1),
 (44,44,NULL,'2014-11-21 12:05:25',1,'2014-11-21 12:05:25',0,'2014-11-21 12:05:25',0,'2014-11-21 12:05:25','2014-11-21 12:05:25',NULL,NULL,1),
 (45,45,NULL,'2014-11-21 12:05:45',1,'2014-11-21 12:05:45',0,'2014-11-21 12:05:45',0,'2014-11-21 12:05:45','2014-11-21 12:05:45',NULL,NULL,1),
 (46,46,NULL,'2014-11-21 12:06:01',1,'2014-11-21 12:06:01',0,'2014-11-21 12:06:01',0,'2014-11-21 12:06:01','2014-11-21 12:06:01',NULL,NULL,1),
 (47,47,NULL,'2014-11-21 12:06:15',1,'2014-11-21 12:06:15',0,'2014-11-21 12:06:15',0,'2014-11-21 12:06:15','2014-11-21 12:06:15',NULL,NULL,1),
 (48,48,NULL,'2014-11-21 12:06:26',1,'2014-11-21 12:06:26',0,'2014-11-21 12:06:26',0,'2014-11-21 12:06:26','2014-11-21 12:06:26',NULL,NULL,1),
 (49,49,NULL,'2014-11-21 12:06:36',1,'2014-11-21 12:06:36',0,'2014-11-21 12:06:36',0,'2014-11-21 12:06:36','2014-11-21 12:06:36',NULL,NULL,1),
 (50,50,NULL,'2014-11-21 12:06:49',1,'2014-11-21 12:06:49',0,'2014-11-21 12:06:49',0,'2014-11-21 12:06:49','2014-11-21 12:06:49',NULL,NULL,1),
 (51,51,NULL,'2014-11-21 12:06:58',1,'2014-11-21 12:06:58',0,'2014-11-21 12:06:58',0,'2014-11-21 12:06:58','2014-11-21 12:06:58',NULL,NULL,1),
 (52,52,NULL,'2014-11-21 12:07:13',1,'2014-11-21 12:07:13',0,'2014-11-21 12:07:13',0,'2014-11-21 12:07:13','2014-11-21 12:07:13',NULL,NULL,1),
 (53,53,NULL,'2014-11-21 12:07:31',1,'2014-11-21 12:07:31',0,'2014-11-21 12:07:31',0,'2014-11-21 12:07:31','2014-11-21 12:07:31',NULL,NULL,1),
 (54,54,NULL,'2014-11-21 12:07:53',1,'2014-11-21 12:07:53',0,'2014-11-21 12:07:53',0,'2014-11-21 12:07:53','2014-11-21 12:07:53',NULL,NULL,1),
 (55,55,NULL,'2014-11-21 12:08:04',1,'2014-11-21 12:08:04',0,'2014-11-21 12:08:04',0,'2014-11-21 12:08:04','2014-11-21 12:08:04',NULL,NULL,1),
 (56,56,NULL,'2014-11-24 11:49:11',1,'2014-11-24 11:49:11',1,'2014-11-24 11:49:11',0,'2014-11-24 11:49:11','2014-11-24 11:49:11',NULL,NULL,1),
 (57,57,NULL,'2014-11-24 11:49:23',1,'2014-11-24 11:49:23',1,'2014-11-24 11:49:23',0,'2014-11-24 11:49:23','2014-11-24 11:49:23',NULL,NULL,1),
 (58,58,NULL,'2014-11-24 11:49:35',1,'2014-11-24 11:49:35',1,'2014-11-24 11:49:35',0,'2014-11-24 11:49:35','2014-11-24 11:49:35',NULL,NULL,1),
 (59,59,NULL,'2014-11-24 11:49:49',1,'2014-11-24 11:49:49',1,'2014-11-24 11:49:49',0,'2014-11-24 11:49:49','2014-11-24 11:49:49',NULL,NULL,1),
 (60,60,NULL,'2014-11-24 11:50:01',1,'2014-11-24 11:50:01',1,'2014-11-24 11:50:01',0,'2014-11-24 11:50:01','2014-11-24 11:50:01',NULL,NULL,1),
 (61,61,NULL,'2014-11-24 11:50:10',1,'2014-11-24 11:50:10',1,'2014-11-24 11:50:10',0,'2014-11-24 11:50:10','2014-11-24 11:50:10',NULL,NULL,1),
 (62,62,NULL,'2014-11-24 12:08:59',1,'2014-11-24 12:08:59',1,'2014-11-24 12:08:59',0,'2014-11-24 12:08:59','2014-11-24 12:08:59',NULL,NULL,1),
 (63,63,NULL,'2014-11-24 12:09:10',1,'2014-11-24 12:09:10',1,'2014-11-24 12:09:10',0,'2014-11-24 12:09:10','2014-11-24 12:09:10',NULL,NULL,1),
 (64,64,NULL,'2014-11-24 12:09:19',1,'2014-11-24 12:09:19',1,'2014-11-24 12:09:19',0,'2014-11-24 12:09:19','2014-11-24 12:09:19',NULL,NULL,1),
 (65,65,NULL,'2014-11-24 12:09:31',1,'2014-11-24 12:09:31',1,'2014-11-24 12:09:31',0,'2014-11-24 12:09:31','2014-11-24 12:09:31',NULL,NULL,1),
 (66,66,NULL,'2014-11-24 12:09:41',1,'2014-11-24 12:09:41',1,'2014-11-24 12:09:41',0,'2014-11-24 12:09:41','2014-11-24 12:09:41',NULL,NULL,1),
 (67,67,NULL,'2014-11-24 14:18:30',1,'2014-11-24 14:18:30',1,'2014-11-24 14:18:30',0,'2014-11-24 14:18:30','2014-11-24 14:18:30',NULL,NULL,1),
 (68,68,NULL,'2014-11-24 14:18:39',1,'2014-11-24 14:18:39',1,'2014-11-24 14:18:39',0,'2014-11-24 14:18:39','2014-11-24 14:18:39',NULL,NULL,1),
 (69,69,NULL,'2014-11-24 14:18:48',1,'2014-11-24 14:18:48',1,'2014-11-24 14:18:48',0,'2014-11-24 14:18:48','2014-11-24 14:18:48',NULL,NULL,1),
 (70,70,NULL,'2014-11-24 14:18:58',1,'2014-11-24 14:18:58',1,'2014-11-24 14:18:58',0,'2014-11-24 14:18:58','2014-11-24 14:18:58',NULL,NULL,1),
 (71,71,NULL,'2014-11-24 14:19:09',1,'2014-11-24 14:19:09',1,'2014-11-24 14:19:09',0,'2014-11-24 14:19:09','2014-11-24 14:19:09',NULL,NULL,1),
 (72,72,NULL,'2014-11-24 14:19:22',1,'2014-11-24 14:19:22',1,'2014-11-24 14:19:22',0,'2014-11-24 14:19:22','2014-11-24 14:19:22',NULL,NULL,1),
 (73,73,NULL,'2014-11-24 14:19:30',1,'2014-11-24 14:19:30',1,'2014-11-24 14:19:30',0,'2014-11-24 14:19:30','2014-11-24 14:19:30',NULL,NULL,1),
 (74,74,NULL,'2014-11-24 14:19:37',1,'2014-11-24 14:19:37',1,'2014-11-24 14:19:37',0,'2014-11-24 14:19:37','2014-11-24 14:19:37',NULL,NULL,1),
 (75,75,NULL,'2014-11-24 14:19:50',1,'2014-11-24 14:19:50',1,'2014-11-24 14:19:50',0,'2014-11-24 14:19:50','2014-11-24 14:19:50',NULL,NULL,1),
 (76,76,NULL,'2014-11-24 14:19:59',1,'2014-11-24 14:19:59',1,'2014-11-24 14:19:59',0,'2014-11-24 14:19:59','2014-11-24 14:19:59',NULL,NULL,1),
 (77,77,NULL,'2014-11-24 14:20:08',1,'2014-11-24 14:20:08',1,'2014-11-24 14:20:08',0,'2014-11-24 14:20:08','2014-11-24 14:20:08',NULL,NULL,1),
 (78,78,NULL,'2014-11-24 14:20:16',1,'2014-11-24 14:20:16',1,'2014-11-24 14:20:16',0,'2014-11-24 14:20:16','2014-11-24 14:20:16',NULL,NULL,1),
 (79,79,NULL,'2014-11-24 14:58:26',1,'2014-11-24 14:58:26',1,'2014-11-24 14:58:26',0,'2014-11-24 14:58:26','2014-11-24 14:58:26',NULL,NULL,1),
 (80,80,NULL,'2014-11-24 14:58:36',1,'2014-11-24 14:58:36',1,'2014-11-24 14:58:36',0,'2014-11-24 14:58:36','2014-11-24 14:58:36',NULL,NULL,1),
 (81,81,NULL,'2014-11-24 14:58:47',1,'2014-11-24 14:58:47',1,'2014-11-24 14:58:47',0,'2014-11-24 14:58:47','2014-11-24 14:58:47',NULL,NULL,1),
 (82,82,NULL,'2014-11-24 14:58:56',1,'2014-11-24 14:58:56',1,'2014-11-24 14:58:56',0,'2014-11-24 14:58:56','2014-11-24 14:58:56',NULL,NULL,1),
 (83,83,NULL,'2014-11-24 14:59:06',1,'2014-11-24 14:59:06',1,'2014-11-24 14:59:06',0,'2014-11-24 14:59:06','2014-11-24 14:59:06',NULL,NULL,1),
 (84,84,NULL,'2014-11-24 15:39:21',1,'2014-11-24 15:39:21',1,'2014-11-24 15:39:21',0,'2014-11-24 15:39:21','2014-11-24 15:39:21',NULL,NULL,1),
 (85,85,NULL,'2014-11-24 15:39:32',1,'2014-11-24 15:39:32',1,'2014-11-24 15:39:32',0,'2014-11-24 15:39:32','2014-11-24 15:39:32',NULL,NULL,1);
/*!40000 ALTER TABLE `code_system_entity_version` ENABLE KEYS */;


--
-- Definition of table `code_system_entity_version_association`
--

DROP TABLE IF EXISTS `code_system_entity_version_association`;
CREATE TABLE `code_system_entity_version_association` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeSystemEntityVersionId1` bigint(20) NOT NULL,
  `codeSystemEntityVersionId2` bigint(20) NOT NULL,
  `leftId` bigint(20) DEFAULT NULL,
  `associationTypeId` bigint(20) NOT NULL,
  `associationKind` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `statusDate` datetime NOT NULL,
  `insertTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `associationTypeId` (`associationTypeId`),
  KEY `codeSystemEntityVersionId2` (`codeSystemEntityVersionId2`),
  KEY `codeSystemEntityVersionId1` (`codeSystemEntityVersionId1`),
  CONSTRAINT `FK_code_system_entity_version_association_3` FOREIGN KEY (`associationTypeId`) REFERENCES `association_type` (`codeSystemEntityVersionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_entity_version_association`
--

/*!40000 ALTER TABLE `code_system_entity_version_association` DISABLE KEYS */;
/*!40000 ALTER TABLE `code_system_entity_version_association` ENABLE KEYS */;


--
-- Definition of table `code_system_metadata_value`
--

DROP TABLE IF EXISTS `code_system_metadata_value`;
CREATE TABLE `code_system_metadata_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parameterValue` text NOT NULL,
  `codeSystemEntityVersionId` bigint(20) DEFAULT NULL,
  `metadataParameterId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `metadataParameterId` (`metadataParameterId`),
  KEY `codeSystemEntityVersionId` (`codeSystemEntityVersionId`),
  CONSTRAINT `FK_ev_parameter_value_md_param` FOREIGN KEY (`metadataParameterId`) REFERENCES `metadata_parameter` (`id`),
  CONSTRAINT `FK_ev_paramr_value_ev` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_entity_version` (`versionId`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_metadata_value`
--

/*!40000 ALTER TABLE `code_system_metadata_value` DISABLE KEYS */;
INSERT INTO `code_system_metadata_value` (`id`,`parameterValue`,`codeSystemEntityVersionId`,`metadataParameterId`) VALUES 
 (7,'123.1.2.3',5,2),
 (11,'legal32',5,1),
 (12,'oid',7,2),
 (13,'test',5,3),
 (14,'',5,4),
 (15,'',6,4),
 (16,'',7,4),
 (17,'',84,4),
 (18,'',85,4),
 (19,'',5,5),
 (20,'',6,5),
 (21,'',7,5),
 (22,'',84,5),
 (23,'',85,5),
 (24,'',5,6),
 (25,'',6,6),
 (26,'',7,6),
 (27,'',84,6),
 (28,'',85,6),
 (29,'',5,7),
 (30,'',6,7),
 (31,'',7,7),
 (32,'',84,7),
 (33,'',85,7),
 (34,'',5,8),
 (35,'',6,8),
 (36,'',7,8),
 (37,'',84,8),
 (38,'',85,8),
 (39,'',5,9),
 (40,'',6,9),
 (41,'',7,9),
 (42,'',84,9),
 (43,'',85,9),
 (44,'',5,10),
 (45,'',6,10),
 (46,'',7,10),
 (47,'',84,10),
 (48,'',85,10),
 (49,'',5,11),
 (50,'',6,11),
 (51,'',7,11),
 (52,'',84,11),
 (53,'',85,11),
 (54,'',5,12),
 (55,'',6,12),
 (56,'',7,12),
 (57,'',84,12),
 (58,'',85,12);
/*!40000 ALTER TABLE `code_system_metadata_value` ENABLE KEYS */;


--
-- Definition of table `code_system_version`
--

DROP TABLE IF EXISTS `code_system_version`;
CREATE TABLE `code_system_version` (
  `versionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeSystemId` bigint(20) NOT NULL,
  `previousVersionID` bigint(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `statusDate` datetime NOT NULL,
  `releaseDate` datetime DEFAULT NULL,
  `expirationDate` datetime DEFAULT NULL,
  `source` text,
  `description` text,
  `preferredLanguageCd` varchar(30) DEFAULT 'de-DE',
  `oid` varchar(100) DEFAULT NULL,
  `licenceHolder` text,
  `underLicence` tinyint(1) DEFAULT NULL,
  `insertTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `validityRange` bigint(20) DEFAULT '238',
  `lastChangeDate` datetime DEFAULT NULL,
  `availableLanguages` text,
  PRIMARY KEY (`versionId`),
  KEY `codeSystemId` (`codeSystemId`),
  CONSTRAINT `FK_code_system_version_code_system` FOREIGN KEY (`codeSystemId`) REFERENCES `code_system` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_version`
--

/*!40000 ALTER TABLE `code_system_version` DISABLE KEYS */;
INSERT INTO `code_system_version` (`versionId`,`codeSystemId`,`previousVersionID`,`name`,`status`,`statusDate`,`releaseDate`,`expirationDate`,`source`,`description`,`preferredLanguageCd`,`oid`,`licenceHolder`,`underLicence`,`insertTimestamp`,`validityRange`,`lastChangeDate`,`availableLanguages`) VALUES 
 (1,1,NULL,'CPI v1',1,'2014-11-17 08:46:34',NULL,NULL,NULL,NULL,'',NULL,NULL,0,'2014-11-17 08:46:34',4,'2014-11-24 15:39:32',NULL),
 (4,2,NULL,'RI v1',1,'2014-11-17 08:52:10',NULL,NULL,NULL,NULL,'',NULL,NULL,0,'2014-11-17 08:52:10',4,'2014-12-04 09:36:50',NULL),
 (5,4,NULL,'healthcareFacilityTypeCode',1,'2014-11-20 15:39:41',NULL,NULL,NULL,NULL,'','2.16.756.5.30.1.127.3.2.1.19',NULL,0,'2014-11-24 11:43:10',4,'2014-11-25 15:13:09',NULL),
 (6,5,NULL,'authorSpeciality',1,'2014-11-20 15:42:07',NULL,NULL,NULL,NULL,'','2.16.756.5.30.1.127.3.2.1.5',NULL,0,'2014-11-24 11:43:10',4,'2014-11-25 15:12:19',NULL),
 (7,6,NULL,'practiceSettingCode',1,'2014-11-24 11:48:50',NULL,NULL,NULL,NULL,'',NULL,NULL,0,'2014-11-24 11:48:50',4,'2014-11-25 15:12:34',NULL),
 (8,7,NULL,'Vertraulichkeitsstufen',1,'2014-11-24 14:58:04',NULL,NULL,NULL,NULL,'',NULL,NULL,0,'2014-11-24 14:58:04',4,'2014-11-25 15:12:50',NULL);
/*!40000 ALTER TABLE `code_system_version` ENABLE KEYS */;


--
-- Definition of table `code_system_version_entity_membership`
--

DROP TABLE IF EXISTS `code_system_version_entity_membership`;
CREATE TABLE `code_system_version_entity_membership` (
  `codeSystemVersionId` bigint(20) NOT NULL,
  `codeSystemEntityId` bigint(20) NOT NULL,
  `isAxis` tinyint(1) DEFAULT '0',
  `isMainClass` tinyint(1) DEFAULT '0',
  `orderNr` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`codeSystemVersionId`,`codeSystemEntityId`),
  KEY `codeSystemEntityId` (`codeSystemEntityId`),
  KEY `codeSystemVersionId` (`codeSystemVersionId`),
  CONSTRAINT `FK_TermVersionMembership_Entity` FOREIGN KEY (`codeSystemEntityId`) REFERENCES `code_system_entity` (`id`),
  CONSTRAINT `FK_TermVersionMembership_VocabularyVersion` FOREIGN KEY (`codeSystemVersionId`) REFERENCES `code_system_version` (`versionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_version_entity_membership`
--

/*!40000 ALTER TABLE `code_system_version_entity_membership` DISABLE KEYS */;
INSERT INTO `code_system_version_entity_membership` (`codeSystemVersionId`,`codeSystemEntityId`,`isAxis`,`isMainClass`,`orderNr`) VALUES 
 (1,5,0,1,NULL),
 (1,6,0,1,NULL),
 (1,7,0,1,NULL),
 (1,84,0,1,NULL),
 (1,85,0,1,NULL),
 (4,10,0,1,NULL),
 (4,11,0,1,NULL),
 (4,12,0,1,NULL),
 (4,13,0,1,NULL),
 (4,14,0,1,NULL),
 (4,15,0,1,NULL),
 (5,17,0,1,NULL),
 (5,18,0,1,NULL),
 (5,19,0,1,NULL),
 (5,20,0,1,NULL),
 (5,21,0,1,NULL),
 (5,22,0,1,NULL),
 (5,23,0,1,NULL),
 (5,24,0,1,NULL),
 (5,25,0,1,NULL),
 (5,26,0,1,NULL),
 (5,27,0,1,NULL),
 (5,28,0,1,NULL),
 (5,29,0,1,NULL),
 (5,30,0,1,NULL),
 (6,16,0,1,NULL),
 (6,31,0,1,NULL),
 (6,32,0,1,NULL),
 (6,33,0,1,NULL),
 (6,34,0,1,NULL),
 (6,35,0,1,NULL),
 (6,36,0,1,NULL),
 (6,37,0,1,NULL),
 (6,38,0,1,NULL),
 (6,39,0,1,NULL),
 (6,40,0,1,NULL),
 (6,41,0,1,NULL),
 (6,42,0,1,NULL),
 (6,43,0,1,NULL),
 (6,44,0,1,NULL),
 (6,45,0,1,NULL),
 (6,46,0,1,NULL),
 (6,47,0,1,NULL),
 (6,48,0,1,NULL),
 (6,49,0,1,NULL),
 (6,50,0,1,NULL),
 (6,51,0,1,NULL),
 (6,52,0,1,NULL),
 (6,53,0,1,NULL),
 (6,54,0,1,NULL),
 (6,55,0,1,NULL),
 (7,56,0,1,NULL),
 (7,57,0,1,NULL),
 (7,58,0,1,NULL),
 (7,59,0,1,NULL),
 (7,60,0,1,NULL),
 (7,61,0,1,NULL),
 (7,62,0,1,NULL),
 (7,63,0,1,NULL),
 (7,64,0,1,NULL),
 (7,65,0,1,NULL),
 (7,66,0,1,NULL),
 (7,67,0,1,NULL),
 (7,68,0,1,NULL),
 (7,69,0,1,NULL),
 (7,70,0,1,NULL),
 (7,71,0,1,NULL),
 (7,72,0,1,NULL),
 (7,73,0,1,NULL),
 (7,74,0,1,NULL),
 (7,75,0,1,NULL),
 (7,76,0,1,NULL),
 (7,77,0,1,NULL),
 (7,78,0,1,NULL),
 (8,79,0,1,NULL),
 (8,80,0,1,NULL),
 (8,81,0,1,NULL),
 (8,82,0,1,NULL),
 (8,83,0,1,NULL);
/*!40000 ALTER TABLE `code_system_version_entity_membership` ENABLE KEYS */;


--
-- Definition of table `concept_value_set_membership`
--

DROP TABLE IF EXISTS `concept_value_set_membership`;
CREATE TABLE `concept_value_set_membership` (
  `codeSystemEntityVersionId` bigint(20) NOT NULL,
  `valuesetVersionId` bigint(20) NOT NULL,
  `status` int(10) unsigned NOT NULL,
  `statusDate` datetime NOT NULL,
  `orderNr` bigint(20) unsigned NOT NULL DEFAULT '0',
  `isStructureEntry` tinyint(1) NOT NULL DEFAULT '0',
  `valueOverride` varchar(100) DEFAULT NULL,
  `description` text,
  `meaning` text,
  `hints` text,
  PRIMARY KEY (`codeSystemEntityVersionId`,`valuesetVersionId`),
  KEY `codeSystemEntityVersionId` (`codeSystemEntityVersionId`),
  KEY `valuesetVersionId` (`valuesetVersionId`),
  CONSTRAINT `FK_TermValueSetAssociation_EntityVersion` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_entity_version` (`versionId`),
  CONSTRAINT `FK_TermValueSetAssociation_ValueSetVersion` FOREIGN KEY (`valuesetVersionId`) REFERENCES `value_set_version` (`versionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `concept_value_set_membership`
--

/*!40000 ALTER TABLE `concept_value_set_membership` DISABLE KEYS */;
/*!40000 ALTER TABLE `concept_value_set_membership` ENABLE KEYS */;


--
-- Definition of table `domain`
--

DROP TABLE IF EXISTS `domain`;
CREATE TABLE `domain` (
  `domainId` bigint(20) NOT NULL AUTO_INCREMENT,
  `domainName` varchar(50) NOT NULL,
  `domainOid` varchar(50) DEFAULT NULL,
  `description` text,
  `displayText` text,
  `isOptional` tinyint(1) DEFAULT NULL,
  `defaultValue` varchar(60) DEFAULT NULL,
  `domainType` varchar(50) DEFAULT NULL,
  `displayOrder` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`domainId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `domain`
--

/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` (`domainId`,`domainName`,`domainOid`,`description`,`displayText`,`isOptional`,`defaultValue`,`domainType`,`displayOrder`) VALUES 
 (1,'ISO_639_1_Language_Codes',NULL,'List of ISO 639-1 codes',NULL,NULL,'33',NULL,187),
 (2,'Validity Domain',NULL,'',NULL,NULL,'system',NULL,186),
 (3,'Display Order',NULL,'Domäne, wie Domänen sortiert werden können.',NULL,NULL,NULL,NULL,187),
 (4,'ImportFormats_CS',NULL,'Formate für den Import',NULL,NULL,NULL,NULL,186),
 (5,'ExportFormats',NULL,'Formate für den Export',NULL,NULL,'196',NULL,187),
 (6,'CodesystemType',NULL,'Typ eines Codesystems',NULL,NULL,NULL,NULL,187),
 (7,'MetadataParameter-Type',NULL,'Typ eines Metadata-Parameters',NULL,NULL,NULL,NULL,187),
 (8,'CodesystemTaxonomy',NULL,'Taxonomie über Codesysteme',NULL,NULL,NULL,NULL,186),
 (9,'Valueset_ValidityRange',NULL,NULL,NULL,NULL,NULL,NULL,186),
 (12,'ImportFormats_VS',NULL,'Formate für den Import',NULL,NULL,NULL,NULL,186),
 (13,'statusConceptVisibility',NULL,NULL,NULL,NULL,NULL,NULL,186),
 (14,'statusConceptDeactivated',NULL,NULL,NULL,NULL,NULL,NULL,186),
 (15,'status',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
 (16,'Datatypes',NULL,NULL,'Datentypen',NULL,NULL,NULL,187);
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;


--
-- Definition of table `domain_value`
--

DROP TABLE IF EXISTS `domain_value`;
CREATE TABLE `domain_value` (
  `domainValueId` bigint(20) NOT NULL AUTO_INCREMENT,
  `domainId` bigint(20) NOT NULL,
  `domainCode` varchar(50) NOT NULL,
  `domainDisplay` varchar(100) NOT NULL,
  `attribut1Classname` varchar(50) DEFAULT NULL,
  `attribut1Value` varchar(50) DEFAULT NULL,
  `orderNo` int(11) DEFAULT NULL,
  `imageFile` text,
  PRIMARY KEY (`domainValueId`),
  KEY `domainId` (`domainId`),
  CONSTRAINT `FK_domain_values_domain` FOREIGN KEY (`domainId`) REFERENCES `domain` (`domainId`)
) ENGINE=InnoDB AUTO_INCREMENT=271 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `domain_value`
--

/*!40000 ALTER TABLE `domain_value` DISABLE KEYS */;
INSERT INTO `domain_value` (`domainValueId`,`domainId`,`domainCode`,`domainDisplay`,`attribut1Classname`,`attribut1Value`,`orderNo`,`imageFile`) VALUES 
 (1,1,'aa','Afar',NULL,NULL,NULL,NULL),
 (2,1,'ab','Abkhazian',NULL,NULL,NULL,NULL),
 (3,1,'ae','Avestan',NULL,NULL,NULL,NULL),
 (4,1,'af','Afrikaans',NULL,NULL,NULL,NULL),
 (5,1,'ak','Akan',NULL,NULL,NULL,NULL),
 (6,1,'am','Amharic',NULL,NULL,NULL,NULL),
 (7,1,'an','Aragonese',NULL,NULL,NULL,NULL),
 (8,1,'ar','Arabic',NULL,NULL,NULL,NULL),
 (9,1,'as','Assamese',NULL,NULL,NULL,NULL),
 (10,1,'av','Avaric',NULL,NULL,NULL,NULL),
 (11,1,'ay','Aymara',NULL,NULL,NULL,NULL),
 (12,1,'az','Azerbaijani',NULL,NULL,NULL,NULL),
 (13,1,'ba','Bashkir',NULL,NULL,NULL,NULL),
 (14,1,'be','Belarusian',NULL,NULL,NULL,NULL),
 (15,1,'bg','Bulgarian',NULL,NULL,NULL,NULL),
 (16,1,'bh','Bihari',NULL,NULL,NULL,NULL),
 (17,1,'bi','Bislama',NULL,NULL,NULL,NULL),
 (18,1,'bm','Bambara',NULL,NULL,NULL,NULL),
 (19,1,'bn','Bengali',NULL,NULL,NULL,NULL),
 (20,1,'bo','Tibetan',NULL,NULL,NULL,NULL),
 (21,1,'br','Breton',NULL,NULL,NULL,NULL),
 (22,1,'bs','Bosnian',NULL,NULL,NULL,NULL),
 (23,1,'ca','Catalan, Valencian',NULL,NULL,NULL,NULL),
 (24,1,'ce','Chechen',NULL,NULL,NULL,NULL),
 (25,1,'ch','Chamorro',NULL,NULL,NULL,NULL),
 (26,1,'co','Corsican',NULL,NULL,NULL,NULL),
 (27,1,'cr','Cree',NULL,NULL,NULL,NULL),
 (28,1,'cs','Czech',NULL,NULL,NULL,NULL),
 (29,1,'cu','Church Slavic, Old Slavonic, Church Slavonic, Old Bulgarian, Old Church Slavonic',NULL,NULL,NULL,NULL),
 (30,1,'cv','Chuvash',NULL,NULL,NULL,NULL),
 (31,1,'cy','Welsh',NULL,NULL,NULL,NULL),
 (32,1,'da','Danish',NULL,NULL,NULL,NULL),
 (33,1,'de','German',NULL,NULL,NULL,NULL),
 (34,1,'dv','Divehi, Dhivehi, Maldivian',NULL,NULL,NULL,NULL),
 (35,1,'dz','Dzongkha',NULL,NULL,NULL,NULL),
 (36,1,'ee','Ewe',NULL,NULL,NULL,NULL),
 (37,1,'el','Modern Greek',NULL,NULL,NULL,NULL),
 (38,1,'en','English',NULL,NULL,NULL,NULL),
 (39,1,'eo','Esperanto',NULL,NULL,NULL,NULL),
 (40,1,'es','Spanish, Castilian',NULL,NULL,NULL,NULL),
 (41,1,'et','Estonian',NULL,NULL,NULL,NULL),
 (42,1,'eu','Basque',NULL,NULL,NULL,NULL),
 (43,1,'fa','Persian',NULL,NULL,NULL,NULL),
 (44,1,'ff','Fulah',NULL,NULL,NULL,NULL),
 (45,1,'fi','Finnish',NULL,NULL,NULL,NULL),
 (46,1,'fj','Fijian',NULL,NULL,NULL,NULL),
 (47,1,'fo','Faroese',NULL,NULL,NULL,NULL),
 (48,1,'fr','French',NULL,NULL,NULL,NULL),
 (49,1,'fy','Western Frisian',NULL,NULL,NULL,NULL),
 (50,1,'ga','Irish',NULL,NULL,NULL,NULL),
 (51,1,'gd','Gaelic, Scottish Gaelic',NULL,NULL,NULL,NULL),
 (52,1,'gl','Galician',NULL,NULL,NULL,NULL),
 (53,1,'gn','Guaraní',NULL,NULL,NULL,NULL),
 (54,1,'gu','Gujarati',NULL,NULL,NULL,NULL),
 (55,1,'gv','Manx',NULL,NULL,NULL,NULL),
 (56,1,'ha','Hausa',NULL,NULL,NULL,NULL),
 (57,1,'he','Modern Hebrew',NULL,NULL,NULL,NULL),
 (58,1,'hi','Hindi',NULL,NULL,NULL,NULL),
 (59,1,'ho','Hiri Motu',NULL,NULL,NULL,NULL),
 (60,1,'hr','Croatian',NULL,NULL,NULL,NULL),
 (61,1,'ht','Haitian, Haitian Creole',NULL,NULL,NULL,NULL),
 (62,1,'hu','Hungarian',NULL,NULL,NULL,NULL),
 (63,1,'hy','Armenian',NULL,NULL,NULL,NULL),
 (64,1,'hz','Herero',NULL,NULL,NULL,NULL),
 (65,1,'ia','Interlingua (International Auxiliary Language Association)',NULL,NULL,NULL,NULL),
 (66,1,'id','Indonesian',NULL,NULL,NULL,NULL),
 (67,1,'ie','Interlingue, Occidental',NULL,NULL,NULL,NULL),
 (68,1,'ig','Igbo',NULL,NULL,NULL,NULL),
 (69,1,'ii','Sichuan Yi, Nuosu',NULL,NULL,NULL,NULL),
 (70,1,'ik','Inupiaq',NULL,NULL,NULL,NULL),
 (71,1,'io','Ido',NULL,NULL,NULL,NULL),
 (72,1,'is','Icelandic',NULL,NULL,NULL,NULL),
 (73,1,'it','Italian',NULL,NULL,NULL,NULL),
 (74,1,'iu','Inuktitut',NULL,NULL,NULL,NULL),
 (75,1,'ja','Japanese',NULL,NULL,NULL,NULL),
 (76,1,'jv','Javanese',NULL,NULL,NULL,NULL),
 (77,1,'ka','Georgian',NULL,NULL,NULL,NULL),
 (78,1,'kg','Kongo',NULL,NULL,NULL,NULL),
 (79,1,'ki','Kikuyu, Gikuyu',NULL,NULL,NULL,NULL),
 (80,1,'kj','Kwanyama, Kuanyama',NULL,NULL,NULL,NULL),
 (81,1,'kk','Kazakh',NULL,NULL,NULL,NULL),
 (82,1,'kl','Kalaallisut, Greenlandic',NULL,NULL,NULL,NULL),
 (83,1,'km','Central Khmer',NULL,NULL,NULL,NULL),
 (84,1,'kn','Kannada',NULL,NULL,NULL,NULL),
 (85,1,'ko','Korean',NULL,NULL,NULL,NULL),
 (86,1,'kr','Kanuri',NULL,NULL,NULL,NULL),
 (87,1,'ks','Kashmiri',NULL,NULL,NULL,NULL),
 (88,1,'ku','Kurdish',NULL,NULL,NULL,NULL),
 (89,1,'kv','Komi',NULL,NULL,NULL,NULL),
 (90,1,'kw','Cornish',NULL,NULL,NULL,NULL),
 (91,1,'ky','Kirghiz, Kyrgyz',NULL,NULL,NULL,NULL),
 (92,1,'la','Latin',NULL,NULL,NULL,NULL),
 (93,1,'lb','Luxembourgish, Letzeburgesch',NULL,NULL,NULL,NULL),
 (94,1,'lg','Ganda',NULL,NULL,NULL,NULL),
 (95,1,'li','Limburgish, Limburgan, Limburger',NULL,NULL,NULL,NULL),
 (96,1,'ln','Lingala',NULL,NULL,NULL,NULL),
 (97,1,'lo','Lao',NULL,NULL,NULL,NULL),
 (98,1,'lt','Lithuanian',NULL,NULL,NULL,NULL),
 (99,1,'lu','Luba-Katanga',NULL,NULL,NULL,NULL),
 (100,1,'lv','Latvian',NULL,NULL,NULL,NULL),
 (101,1,'mg','Malagasy',NULL,NULL,NULL,NULL),
 (102,1,'mh','Marshallese',NULL,NULL,NULL,NULL),
 (103,1,'mi','Maori',NULL,NULL,NULL,NULL),
 (104,1,'mk','Macedonian',NULL,NULL,NULL,NULL),
 (105,1,'ml','Malayalam',NULL,NULL,NULL,NULL),
 (106,1,'mn','Mongolian',NULL,NULL,NULL,NULL),
 (107,1,'mr','Marathi',NULL,NULL,NULL,NULL),
 (108,1,'ms','Malay',NULL,NULL,NULL,NULL),
 (109,1,'mt','Maltese',NULL,NULL,NULL,NULL),
 (110,1,'my','Burmese',NULL,NULL,NULL,NULL),
 (111,1,'na','Nauru',NULL,NULL,NULL,NULL),
 (112,1,'nb','Norwegian Bokmål',NULL,NULL,NULL,NULL),
 (113,1,'nd','North Ndebele',NULL,NULL,NULL,NULL),
 (114,1,'ne','Nepali',NULL,NULL,NULL,NULL),
 (115,1,'ng','Ndonga',NULL,NULL,NULL,NULL),
 (116,1,'nl','Dutch, Flemish',NULL,NULL,NULL,NULL),
 (117,1,'nn','Norwegian Nynorsk',NULL,NULL,NULL,NULL),
 (118,1,'no','Norwegian',NULL,NULL,NULL,NULL),
 (119,1,'nr','South Ndebele',NULL,NULL,NULL,NULL),
 (120,1,'nv','Navajo, Navaho',NULL,NULL,NULL,NULL),
 (121,1,'ny','Chichewa, Chewa, Nyanja',NULL,NULL,NULL,NULL),
 (122,1,'oc','Occitan (after 1500)',NULL,NULL,NULL,NULL),
 (123,1,'oj','Ojibwa',NULL,NULL,NULL,NULL),
 (124,1,'om','Oromo',NULL,NULL,NULL,NULL),
 (125,1,'or','Oriya',NULL,NULL,NULL,NULL),
 (126,1,'os','Ossetian, Ossetic',NULL,NULL,NULL,NULL),
 (127,1,'pa','Panjabi, Punjabi',NULL,NULL,NULL,NULL),
 (128,1,'pi','Pali',NULL,NULL,NULL,NULL),
 (129,1,'pl','Polish',NULL,NULL,NULL,NULL),
 (130,1,'ps','Pashto, Pushto',NULL,NULL,NULL,NULL),
 (131,1,'pt','Portuguese',NULL,NULL,NULL,NULL),
 (132,1,'qu','Quechua',NULL,NULL,NULL,NULL),
 (133,1,'rm','Romansh',NULL,NULL,NULL,NULL),
 (134,1,'rn','Rundi',NULL,NULL,NULL,NULL),
 (135,1,'ro','Romanian, Moldavian, Moldovan',NULL,NULL,NULL,NULL),
 (136,1,'ru','Russian',NULL,NULL,NULL,NULL),
 (137,1,'rw','Kinyarwanda',NULL,NULL,NULL,NULL),
 (138,1,'sa','Sanskrit',NULL,NULL,NULL,NULL),
 (139,1,'sc','Sardinian',NULL,NULL,NULL,NULL),
 (140,1,'sd','Sindhi',NULL,NULL,NULL,NULL),
 (141,1,'se','Northern Sami',NULL,NULL,NULL,NULL),
 (142,1,'sg','Sango',NULL,NULL,NULL,NULL),
 (143,1,'si','Sinhala, Sinhalese',NULL,NULL,NULL,NULL),
 (144,1,'sk','Slovak',NULL,NULL,NULL,NULL),
 (145,1,'sl','Slovene',NULL,NULL,NULL,NULL),
 (146,1,'sm','Samoan',NULL,NULL,NULL,NULL),
 (147,1,'sn','Shona',NULL,NULL,NULL,NULL),
 (148,1,'so','Somali',NULL,NULL,NULL,NULL),
 (149,1,'sq','Albanian',NULL,NULL,NULL,NULL),
 (150,1,'sr','Serbian',NULL,NULL,NULL,NULL),
 (151,1,'ss','Swati',NULL,NULL,NULL,NULL),
 (152,1,'st','Southern Sotho',NULL,NULL,NULL,NULL),
 (153,1,'su','Sundanese',NULL,NULL,NULL,NULL),
 (154,1,'sv','Swedish',NULL,NULL,NULL,NULL),
 (155,1,'sw','Swahili',NULL,NULL,NULL,NULL),
 (156,1,'ta','Tamil',NULL,NULL,NULL,NULL),
 (157,1,'te','Telugu',NULL,NULL,NULL,NULL),
 (158,1,'tg','Tajik',NULL,NULL,NULL,NULL),
 (159,1,'th','Thai',NULL,NULL,NULL,NULL),
 (160,1,'ti','Tigrinya',NULL,NULL,NULL,NULL),
 (161,1,'tk','Turkmen',NULL,NULL,NULL,NULL),
 (162,1,'tl','Tagalog',NULL,NULL,NULL,NULL),
 (163,1,'tn','Tswana',NULL,NULL,NULL,NULL),
 (164,1,'to','Tonga (Tonga Islands)',NULL,NULL,NULL,NULL),
 (165,1,'tr','Turkish',NULL,NULL,NULL,NULL),
 (166,1,'ts','Tsonga',NULL,NULL,NULL,NULL),
 (167,1,'tt','Tatar',NULL,NULL,NULL,NULL),
 (168,1,'tw','Twi',NULL,NULL,NULL,NULL),
 (169,1,'ty','Tahitian',NULL,NULL,NULL,NULL),
 (170,1,'ug','Uighur, Uyghur',NULL,NULL,NULL,NULL),
 (171,1,'uk','Ukrainian',NULL,NULL,NULL,NULL),
 (172,1,'ur','Urdu',NULL,NULL,NULL,NULL),
 (173,1,'uz','Uzbek',NULL,NULL,NULL,NULL),
 (174,1,'ve','Venda',NULL,NULL,NULL,NULL),
 (175,1,'vi','Vietnamese',NULL,NULL,NULL,NULL),
 (176,1,'vo','Volapük',NULL,NULL,NULL,NULL),
 (177,1,'wa','Walloon',NULL,NULL,NULL,NULL),
 (178,1,'wo','Wolof',NULL,NULL,NULL,NULL),
 (179,1,'xh','Xhosa',NULL,NULL,NULL,NULL),
 (180,1,'yi','Yiddish',NULL,NULL,NULL,NULL),
 (181,1,'yo','Yoruba',NULL,NULL,NULL,NULL),
 (182,1,'za','Zhuang, Chuang',NULL,NULL,NULL,NULL),
 (183,1,'zh','Chinese',NULL,NULL,NULL,NULL),
 (184,1,'zu','Zulu',NULL,NULL,NULL,NULL),
 (185,3,'id','nach ID',NULL,NULL,NULL,NULL),
 (186,3,'order','nach Order-Number','','',0,NULL),
 (187,3,'name','nach Name',NULL,NULL,NULL,NULL),
 (188,2,'system','System',NULL,NULL,1,NULL),
 (189,2,'modul','Modul',NULL,NULL,2,NULL),
 (190,2,'service','Service',NULL,NULL,3,NULL),
 (191,2,'usergroup','Benutzergruppe',NULL,NULL,4,NULL),
 (192,2,'user','Benutzer',NULL,NULL,5,NULL),
 (193,4,'2','ClaML',NULL,NULL,2,NULL),
 (194,4,'1','CSV (Kommagetrennte Datei)',NULL,NULL,1,NULL),
 (195,5,'claml','ClaML',NULL,NULL,NULL,NULL),
 (196,5,'csv','CSV (Kommagetrennte Datei)',NULL,NULL,NULL,NULL),
 (197,5,'xml','XML',NULL,NULL,NULL,NULL),
 (200,4,'3','LOINC (txt)',NULL,NULL,3,NULL),
 (201,4,'4','LOINC Beziehungen (txt)',NULL,NULL,4,NULL),
 (202,7,'administrative','Administrativ',NULL,NULL,NULL,NULL),
 (203,7,'descriptive','Charakterisierend/Deskriptiv',NULL,NULL,NULL,NULL),
 (204,7,'medical','Medizinisch',NULL,NULL,NULL,NULL),
 (205,7,'technical','Technisch',NULL,NULL,NULL,NULL),
 (206,6,'vocabulary','Vokabular',NULL,NULL,NULL,NULL),
 (214,6,'nomenclature','Nomenklatur',NULL,NULL,NULL,NULL),
 (215,6,'thesaurus','Thesaurus',NULL,NULL,NULL,NULL),
 (216,6,'terminology','Terminologie',NULL,NULL,NULL,NULL),
 (217,6,'valuelist','Werteliste',NULL,NULL,NULL,NULL),
 (218,6,'ontology','Ontologie',NULL,NULL,NULL,NULL),
 (219,6,'classification','Klassifikation',NULL,NULL,NULL,NULL),
 (234,4,'5','KBV Keytabs',NULL,NULL,5,NULL),
 (235,9,'1','verpflichtend',NULL,'allgemein, Gesetzt, Verordnung',1,NULL),
 (236,9,'2','empfohlen',NULL,'allgemein, Beschluss BGK (ev. GV ELGA GmbH)',2,NULL),
 (237,9,'3','bedingt',NULL,'eingeschränkt',3,NULL),
 (238,9,'4','optional',NULL,'offen',4,NULL),
 (246,4,'8','Sharing Value Sets (SVS)',NULL,NULL,8,NULL),
 (247,4,'7','Leitlinien-Katalog (Österreich)',NULL,NULL,7,NULL),
 (248,4,'6','ICD BMG Österreich',NULL,NULL,6,NULL),
 (249,12,'1','Kommagetrennte Datei (CSV)',NULL,NULL,1,NULL),
 (250,12,'2','Sharing Value Set (SVS)',NULL,NULL,2,NULL),
 (251,13,'0','invisible',NULL,NULL,0,NULL),
 (252,13,'1','visible',NULL,NULL,1,NULL),
 (254,14,'0','deactive',NULL,NULL,0,NULL),
 (255,14,'1','active',NULL,NULL,1,NULL),
 (256,14,'2','deleted',NULL,NULL,2,NULL),
 (257,14,'3','deprecated',NULL,NULL,3,NULL),
 (258,15,'0','inactive',NULL,NULL,0,NULL),
 (259,15,'1','active',NULL,NULL,1,NULL),
 (260,15,'2','removed',NULL,NULL,2,NULL),
 (262,16,'String','String',NULL,NULL,NULL,NULL),
 (263,16,'bool','bool',NULL,NULL,NULL,NULL),
 (264,16,'int','int',NULL,NULL,NULL,NULL),
 (265,16,'float','float',NULL,NULL,NULL,NULL),
 (266,16,'Object','Object',NULL,NULL,NULL,NULL),
 (267,16,'BLOB','BLOB',NULL,NULL,NULL,NULL),
 (268,8,'EPD','EPD-Stammdaten',NULL,NULL,1,NULL),
 (270,8,'MDI','Dokument-Metadaten-Index (DMI)',NULL,NULL,2,NULL);
/*!40000 ALTER TABLE `domain_value` ENABLE KEYS */;


--
-- Definition of table `domain_value_has_code_system`
--

DROP TABLE IF EXISTS `domain_value_has_code_system`;
CREATE TABLE `domain_value_has_code_system` (
  `domain_value_domainValueId` bigint(20) NOT NULL,
  `code_system_id` bigint(20) NOT NULL,
  PRIMARY KEY (`domain_value_domainValueId`,`code_system_id`),
  KEY `fk_domain_value_has_code_system_code_system1` (`code_system_id`),
  KEY `fk_domain_value_has_code_system_domain_value1` (`domain_value_domainValueId`),
  CONSTRAINT `FK_domain_value_has_code_system_1` FOREIGN KEY (`code_system_id`) REFERENCES `code_system` (`id`),
  CONSTRAINT `FK_domain_value_has_code_system_2` FOREIGN KEY (`domain_value_domainValueId`) REFERENCES `domain_value` (`domainValueId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `domain_value_has_code_system`
--

/*!40000 ALTER TABLE `domain_value_has_code_system` DISABLE KEYS */;
INSERT INTO `domain_value_has_code_system` (`domain_value_domainValueId`,`code_system_id`) VALUES 
 (268,1),
 (268,2),
 (270,4),
 (270,5),
 (270,6),
 (270,7);
/*!40000 ALTER TABLE `domain_value_has_code_system` ENABLE KEYS */;


--
-- Definition of table `domainvalue2domainvalue`
--

DROP TABLE IF EXISTS `domainvalue2domainvalue`;
CREATE TABLE `domainvalue2domainvalue` (
  `domainValueId1` bigint(20) NOT NULL,
  `domainValueId2` bigint(20) NOT NULL,
  PRIMARY KEY (`domainValueId1`,`domainValueId2`),
  KEY `fk_domainvalue2domainvalue_domain_value2` (`domainValueId2`),
  CONSTRAINT `FK_domainvalue2domainvalue_1` FOREIGN KEY (`domainValueId1`) REFERENCES `domain_value` (`domainValueId`),
  CONSTRAINT `FK_domainvalue2domainvalue_2` FOREIGN KEY (`domainValueId2`) REFERENCES `domain_value` (`domainValueId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `domainvalue2domainvalue`
--

/*!40000 ALTER TABLE `domainvalue2domainvalue` DISABLE KEYS */;
INSERT INTO `domainvalue2domainvalue` (`domainValueId1`,`domainValueId2`) VALUES 
 (268,270);
/*!40000 ALTER TABLE `domainvalue2domainvalue` ENABLE KEYS */;


--
-- Definition of table `licence_type`
--

DROP TABLE IF EXISTS `licence_type`;
CREATE TABLE `licence_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeSystemVersionId` bigint(20) DEFAULT NULL,
  `typeTxt` text,
  PRIMARY KEY (`id`),
  KEY `codeSystemVersionId` (`codeSystemVersionId`),
  CONSTRAINT `FK_licence_type_vv` FOREIGN KEY (`codeSystemVersionId`) REFERENCES `code_system_version` (`versionId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `licence_type`
--

/*!40000 ALTER TABLE `licence_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `licence_type` ENABLE KEYS */;


--
-- Definition of table `licenced_user`
--

DROP TABLE IF EXISTS `licenced_user`;
CREATE TABLE `licenced_user` (
  `userId` bigint(20) NOT NULL,
  `codeSystemVersionId` bigint(20) NOT NULL,
  `licenceTypeId` bigint(20) DEFAULT NULL,
  `validFrom` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `validTo` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`userId`,`codeSystemVersionId`),
  KEY `userId` (`userId`),
  KEY `codeSystemVersionId` (`codeSystemVersionId`),
  KEY `licenceTypeId` (`licenceTypeId`),
  CONSTRAINT `FK_licenced_user_lt` FOREIGN KEY (`licenceTypeId`) REFERENCES `licence_type` (`id`),
  CONSTRAINT `FK_licenced_user_term_user` FOREIGN KEY (`userId`) REFERENCES `term_user` (`id`),
  CONSTRAINT `FK_licenced_user_vv` FOREIGN KEY (`codeSystemVersionId`) REFERENCES `code_system_version` (`versionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `licenced_user`
--

/*!40000 ALTER TABLE `licenced_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `licenced_user` ENABLE KEYS */;


--
-- Definition of table `metadata_parameter`
--

DROP TABLE IF EXISTS `metadata_parameter`;
CREATE TABLE `metadata_parameter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `paramName` text NOT NULL,
  `paramDatatype` text,
  `metadataParameterType` varchar(30) DEFAULT NULL,
  `codeSystemId` bigint(20) DEFAULT NULL,
  `valueSetId` bigint(20) DEFAULT NULL,
  `languageCd` varchar(30) DEFAULT NULL,
  `description` text,
  `paramNameDisplay` text,
  `maxLength` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_metadata_parameter_1` (`codeSystemId`),
  KEY `FK_metadata_parameter_2` (`valueSetId`),
  CONSTRAINT `FK_metadata_parameter_1` FOREIGN KEY (`codeSystemId`) REFERENCES `code_system` (`id`),
  CONSTRAINT `metadata_parameter_ibfk_1` FOREIGN KEY (`valueSetId`) REFERENCES `value_set` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `metadata_parameter`
--

/*!40000 ALTER TABLE `metadata_parameter` DISABLE KEYS */;
INSERT INTO `metadata_parameter` (`id`,`paramName`,`paramDatatype`,`metadataParameterType`,`codeSystemId`,`valueSetId`,`languageCd`,`description`,`paramNameDisplay`,`maxLength`) VALUES 
 (1,'ComLegal','String','',1,NULL,'de','beschreibender Freitext über die rechtlichen Grundlagen der Gemeinschaft, z.B. mit Verweis auf kantonales Gesundheitsgesetz oder nationales Elektronisches Patientendossiergesetz oder private Vereinbarungen auf Gemeinschaftsebene ','rechtliche Grundlagen für die Gemeinschaft',NULL),
 (2,'ComOID','String','descriptive',1,NULL,'','Jede Gemeinschaft muss eine eindeutige OID erhalten, welche die Gemeinschaft weltweit eindeutig identifiziert. Diese muss im Schweizer OID- Register gelistet sein.','OID der Gemeinschaft',NULL),
 (3,'ComAuthN','BLOB','',1,NULL,'','Jede Gemeinschaft muss mindestens einen Authentisierungsprovider haben, der die SAML Assertions im Namen der Gemeinschaft unterschreibt.\nIn diesem Attribute werden alle aktuell gu?ltigen X.509 Zertifikate hinterlegt.','Authentisierungsprovider der Gemeinschaft',NULL),
 (4,'ComLogo','BLOB','descriptive',1,NULL,'','bildliches Logo der Gemeinschaft im JPG Format','Logo der Gemeinschaft',NULL),
 (5,'ComContact','String','',1,NULL,'','Um mit der Gemeinschaft in direkten Kontakt zu treten werden die Kontaktinformationen im CPI-S hinterlegt. Diese Information wird von anderen Gemeinschaften an Behandelnde und Patienten weitergegeben.\nFür die Zusammenarbeit zwischen den Gemeinschaften ist ein zweiter (technischer) Kontakt zu hinterlegen, der aber nur für interne Zwecke verwendet werden darf.\nZu jedem Kontakt sind mindestens zwei mögliche Kontaktmethoden zu hinterlegen. (Postadresse, E- Mail und Telefon).','Kontaktinformationen der Gemeinschaft',NULL),
 (6,'ComAssert','String','',1,NULL,'','jede Gemeinschaft muss für die Rolle als Stammgemeinschaft einen Assertion Service betreiben, welche die digitalen Signaturen der Rechteattribute vornehmen kann. Die zur Prüfung solcher Signaturen nötigen Informationen werden im CPI-S hinterlegt.','Assertion Authority der Gemeinschaft',NULL),
 (7,'ComXPoint','String','',1,NULL,'','Abrufbare Information für Responding Gateways die die Assertion Provider in den Initiating Gateways kontaktieren wollen','Pointer auf X-Assertion Provider',NULL),
 (8,'ComGW','String','',1,NULL,'','Jede Gemeinschaft muss mindestens einen Gateway betreiben, welcher die Kommunikation zwischen den Gemeinschaften abwickelt.\nIn diesem Attribut werden die URLs der Gateways hinterlegt.','Zugangspunkt der Gemeinschaft',NULL),
 (9,'ComVersion','String','',1,NULL,'','für welche Version der Gateways ist die Gemeinschaft zertifiziert','Version der Gemeinschaft',NULL),
 (10,'ComZert','String','',1,NULL,'','das Datum der erfolgreichen Zertifizierung bzw. Rezertifizierung','Datum der Zertifizierung',NULL),
 (11,'ComName','String','',1,NULL,'','Name der Gemeinschaft. Diese Bezeichnung muss eindeutig und selbsterklärend sein. Sie wird dazu verwendet um die Gemeinschaft im Portal zu benennen und kann von Patienten oder Behandelnden bei der Wahl der Stammgemeinschaft oder beim Definieren von Rechteattributen genutzt werden.','Gemeinschaftsname',NULL),
 (12,'ComInfo','String','',1,NULL,'','allgemeine beschreibende Informationen zur Gemeinschaft (Freitext, max. 500 Zeichen)','Information zur Gemeinschaft',500);
/*!40000 ALTER TABLE `metadata_parameter` ENABLE KEYS */;


--
-- Definition of table `session`
--

DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `termUserId` bigint(20) NOT NULL,
  `lastTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sessionId` text NOT NULL,
  `ipAddress` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `termUserId` (`termUserId`),
  KEY `termUserId_2` (`termUserId`),
  CONSTRAINT `FK_session_term_user` FOREIGN KEY (`termUserId`) REFERENCES `term_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=270 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `session`
--

/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` (`id`,`termUserId`,`lastTimestamp`,`sessionId`,`ipAddress`) VALUES 
 (263,5,'2014-12-16 08:19:15','af789d04-94d4-4e97-b12c-45d89c23082d','127.0.0.1'),
 (269,1,'2014-12-22 11:58:51','c9e0baf7-3eb8-4003-9e6f-da1ad824a0b3','127.0.0.1');
/*!40000 ALTER TABLE `session` ENABLE KEYS */;


--
-- Definition of table `sys_param`
--

DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `validityDomain` bigint(20) DEFAULT NULL,
  `objectId` bigint(20) DEFAULT NULL,
  `modifyLevel` bigint(20) DEFAULT NULL,
  `javaDatatype` text,
  `value` text,
  `description` text,
  PRIMARY KEY (`id`),
  KEY `validityDomain` (`validityDomain`),
  KEY `FK_sys_param_2` (`modifyLevel`),
  CONSTRAINT `FK_sys_param_2` FOREIGN KEY (`modifyLevel`) REFERENCES `domain_value` (`domainValueId`),
  CONSTRAINT `FK_sys_param_domain_value` FOREIGN KEY (`validityDomain`) REFERENCES `domain_value` (`domainValueId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `sys_param`
--

/*!40000 ALTER TABLE `sys_param` DISABLE KEYS */;
INSERT INTO `sys_param` (`id`,`name`,`validityDomain`,`objectId`,`modifyLevel`,`javaDatatype`,`value`,`description`) VALUES 
 (1,'maxPageSize',188,NULL,188,'int','101','Maximale Anzahl an Treffern für z.B. ListCodeSystemConcepts'),
 (2,'maxPageSizeSearch',188,NULL,188,'int','5','Maximale Anzahl an Suchergebnisse pro Seite bei einer Suchanfrage'),
 (3,'mail_sender',188,NULL,188,'string','ehealth@fh-dortmund.de',NULL),
 (4,'mail_host',188,NULL,188,'string','mailout2.fh-dortmund.de',NULL),
 (5,'mail_name',188,NULL,188,'string','Ehealth FH-Dortmund [Terminologieserver]',NULL),
 (6,'mail_user',188,NULL,188,'string','ehealth',NULL),
 (7,'mail_password',188,NULL,188,'string','derEHealthBoSS#',NULL),
 (8,'mail_port',188,NULL,188,'string','587',NULL),
 (9,'weblink',188,NULL,188,'string','http://www.term.mi.fh-dortmund.de:8080/TermAdmin',NULL),
 (10,'fh_dortmund',188,NULL,188,'bool','true','Zusatzcode für FH Dortmund (Login)'),
 (11,'login_type',188,NULL,188,'string','userpass',NULL),
 (12,'db_version',188,NULL,188,'int','3',NULL);
/*!40000 ALTER TABLE `sys_param` ENABLE KEYS */;


--
-- Definition of table `term_user`
--

DROP TABLE IF EXISTS `term_user`;
CREATE TABLE `term_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `passw` text,
  `isAdmin` tinyint(1) NOT NULL DEFAULT '0',
  `salt` text,
  `email` text,
  `userName` text,
  `activated` tinyint(1) DEFAULT NULL,
  `activation_md5` varchar(80) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  `pseudonym` varchar(100) DEFAULT NULL,
  `activation_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `term_user`
--

/*!40000 ALTER TABLE `term_user` DISABLE KEYS */;
INSERT INTO `term_user` (`id`,`name`,`passw`,`isAdmin`,`salt`,`email`,`userName`,`activated`,`activation_md5`,`enabled`,`pseudonym`,`activation_time`) VALUES 
 (1,'user','218289d7a13cdcd8d3755375d78819d8',0,'VD532V09mznL',' ',NULL,1,NULL,1,'',NULL),
 (5,'admin','7006f91fba92af5da76121ea72ff47c6',1,'p9rEwe8j1P34',' ',NULL,1,NULL,1,NULL,'2014-07-25 11:02:16');
/*!40000 ALTER TABLE `term_user` ENABLE KEYS */;


--
-- Definition of table `value_set`
--

DROP TABLE IF EXISTS `value_set`;
CREATE TABLE `value_set` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currentVersionId` bigint(20) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` text,
  `status` int(11) DEFAULT NULL,
  `statusDate` datetime DEFAULT NULL,
  `descriptionEng` text,
  `website` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `value_set`
--

/*!40000 ALTER TABLE `value_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `value_set` ENABLE KEYS */;


--
-- Definition of table `value_set_metadata_value`
--

DROP TABLE IF EXISTS `value_set_metadata_value`;
CREATE TABLE `value_set_metadata_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parameterValue` text NOT NULL,
  `codeSystemEntityVersionId` bigint(20) DEFAULT NULL,
  `valuesetVersionId` bigint(20) DEFAULT NULL,
  `metadataParameterId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `metadataParameterId` (`metadataParameterId`),
  KEY `FK_value_set_metadata_value_2` (`codeSystemEntityVersionId`),
  KEY `FK_value_set_metadata_value_3` (`valuesetVersionId`),
  CONSTRAINT `FK_value_set_metadata_value_2` FOREIGN KEY (`codeSystemEntityVersionId`) REFERENCES `code_system_entity_version` (`versionId`),
  CONSTRAINT `FK_value_set_metadata_value_3` FOREIGN KEY (`valuesetVersionId`) REFERENCES `value_set_version` (`versionId`),
  CONSTRAINT `value_set_metadata_value_ibfk_1` FOREIGN KEY (`metadataParameterId`) REFERENCES `metadata_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `value_set_metadata_value`
--

/*!40000 ALTER TABLE `value_set_metadata_value` DISABLE KEYS */;
/*!40000 ALTER TABLE `value_set_metadata_value` ENABLE KEYS */;


--
-- Definition of table `value_set_version`
--

DROP TABLE IF EXISTS `value_set_version`;
CREATE TABLE `value_set_version` (
  `versionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `valueSetId` bigint(20) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `statusDate` datetime NOT NULL,
  `insertTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `releaseDate` datetime DEFAULT NULL,
  `previousVersionId` bigint(20) DEFAULT NULL,
  `preferredLanguageCd` varchar(30) DEFAULT NULL,
  `oid` varchar(100) DEFAULT NULL,
  `validityRange` bigint(20) DEFAULT '238',
  `name` varchar(100) DEFAULT NULL,
  `lastChangeDate` datetime DEFAULT NULL,
  `virtualCodeSystemVersionId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`versionId`),
  KEY `valueSetId` (`valueSetId`),
  CONSTRAINT `FK_ValueSetVersion_ValueSet` FOREIGN KEY (`valueSetId`) REFERENCES `value_set` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `value_set_version`
--

/*!40000 ALTER TABLE `value_set_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `value_set_version` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
