-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.6.16


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema termsuisse
--

CREATE DATABASE IF NOT EXISTS termsuisse;
USE termsuisse;

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
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system`
--

/*!40000 ALTER TABLE `code_system` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=412269 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_entity`
--

/*!40000 ALTER TABLE `code_system_entity` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=412268 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_entity_version`
--

/*!40000 ALTER TABLE `code_system_entity_version` DISABLE KEYS */;
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
  KEY `codeSystemEntityVersionId1` (`codeSystemEntityVersionId1`)
) ENGINE=InnoDB AUTO_INCREMENT=219673 DEFAULT CHARSET=latin1;

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
) ENGINE=InnoDB AUTO_INCREMENT=3946469 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_metadata_value`
--

/*!40000 ALTER TABLE `code_system_metadata_value` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `code_system_version`
--

/*!40000 ALTER TABLE `code_system_version` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=268 DEFAULT CHARSET=latin1;

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
 (208,8,'diagnosis','Diagnosen',NULL,NULL,1,NULL),
 (209,8,'procedures','Maßnahmen',NULL,NULL,3,NULL),
 (214,6,'nomenclature','Nomenklatur',NULL,NULL,NULL,NULL),
 (215,6,'thesaurus','Thesaurus',NULL,NULL,NULL,NULL),
 (216,6,'terminology','Terminologie',NULL,NULL,NULL,NULL),
 (217,6,'valuelist','Werteliste',NULL,NULL,NULL,NULL),
 (218,6,'ontology','Ontologie',NULL,NULL,NULL,NULL),
 (219,6,'classification','Klassifikation',NULL,NULL,NULL,NULL),
 (220,8,'symptoms','Symptome',NULL,NULL,2,NULL),
 (221,8,'healthstatus','Gesundheitsstatus',NULL,NULL,4,NULL),
 (222,8,'resultattributes','Ergebnisattribute',NULL,NULL,5,NULL),
 (223,8,'substances','Substanzen',NULL,NULL,6,NULL),
 (224,8,'measurements','Maßeinheiten, Verabreichungsformen etc.',NULL,NULL,7,NULL),
 (225,8,'orgtypes','Organisationstypen/Fachgebiete/Rollen',NULL,NULL,8,NULL),
 (226,8,'environments','Umwelt/geographische Regionen',NULL,NULL,9,NULL),
 (227,8,'billing','Abrechnungskataloge',NULL,NULL,10,NULL),
 (228,8,'projects','Projekt- / Verwendungsspezifische',NULL,NULL,11,NULL),
 (229,8,'common','Allgemeine Substanzen',NULL,NULL,1,NULL),
 (230,8,'specimen','Untersuchungsmaterial',NULL,NULL,2,NULL),
 (231,8,'pharmaceutical_product','Pharmazeutisches/biologisches Produkt',NULL,NULL,3,NULL),
 (232,8,'eBPG','eBPG',NULL,NULL,1,NULL),
 (233,8,'KBV_tables','KBV-Tabellen',NULL,NULL,2,NULL),
 (234,4,'5','KBV Keytabs',NULL,NULL,5,NULL),
 (235,9,'1','verpflichtend',NULL,'allgemein, Gesetzt, Verordnung',1,NULL),
 (236,9,'2','empfohlen',NULL,'allgemein, Beschluss BGK (ev. GV ELGA GmbH)',2,NULL),
 (237,9,'3','bedingt',NULL,'eingeschränkt',3,NULL),
 (238,9,'4','optional',NULL,'offen',4,NULL),
 (239,8,'NGC','National Guidenline Clearinghouse',NULL,NULL,NULL,NULL),
 (240,8,'GEKID','GEKID',NULL,NULL,NULL,NULL),
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
 (261,8,'metadata','Metadaten',NULL,NULL,12,NULL),
 (262,16,'String','String',NULL,NULL,NULL,NULL),
 (263,16,'bool','bool',NULL,NULL,NULL,NULL),
 (264,16,'int','int',NULL,NULL,NULL,NULL),
 (265,16,'float','float',NULL,NULL,NULL,NULL),
 (266,16,'Object','Object',NULL,NULL,NULL,NULL),
 (267,16,'BLOB','BLOB',NULL,NULL,NULL,NULL);
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
 (223,229),
 (223,230),
 (223,231),
 (228,232),
 (228,233),
 (228,239),
 (228,240);
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

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
  PRIMARY KEY (`id`),
  KEY `FK_metadata_parameter_1` (`codeSystemId`),
  KEY `FK_metadata_parameter_2` (`valueSetId`),
  CONSTRAINT `FK_metadata_parameter_1` FOREIGN KEY (`codeSystemId`) REFERENCES `code_system` (`id`),
  CONSTRAINT `metadata_parameter_ibfk_1` FOREIGN KEY (`valueSetId`) REFERENCES `value_set` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `metadata_parameter`
--

/*!40000 ALTER TABLE `metadata_parameter` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `session`
--

/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` (`id`,`termUserId`,`lastTimestamp`,`sessionId`,`ipAddress`) VALUES 
 (131,5,'2014-10-23 12:29:16','402209b5-17f3-489e-bdc4-60654956bd12','127.0.0.1'),
 (138,1,'2014-11-13 14:05:44','a70c424f-1a5a-484b-8bf0-26dcd78bc3c7','127.0.0.1');
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;

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
 (11,'login_type',188,NULL,188,'string','userpass',NULL);
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
