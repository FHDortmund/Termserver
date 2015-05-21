-- phpMyAdmin SQL Dump
-- version 3.5.2.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Erstellungszeit: 19. Okt 2013 um 10:40
-- Server Version: 5.5.27
-- PHP-Version: 5.4.7

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `web5db3`
--
CREATE DATABASE `web5db3` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `web5db3`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `action`
--

CREATE TABLE IF NOT EXISTS `action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

--
-- Daten für Tabelle `action`
--

INSERT INTO `action` (`id`, `action`) VALUES
(1, 'ablehnen'),
(2, 'diskutieren'),
(3, 'produzieren'),
(4, 'freigeben'),
(5, 'entsperren'),
(6, 'sperren'),
(7, 'einfrieren'),
(8, 'deaktivieren');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `assigned_term`
--

CREATE TABLE IF NOT EXISTS `assigned_term` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classId` bigint(20) DEFAULT NULL,
  `classname` text,
  `collaborationUserId` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserId` (`collaborationUserId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `class_attribute`
--

CREATE TABLE IF NOT EXISTS `class_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_name` varchar(100) NOT NULL,
  `attribute` varchar(100) NOT NULL,
  `domain_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_class_attribute_domain1_idx` (`domain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `collaborationuser`
--

CREATE TABLE IF NOT EXISTS `collaborationuser` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` text NOT NULL,
  `salt` varchar(128) DEFAULT NULL,
  `name` text,
  `firstName` text,
  `city` varchar(90) DEFAULT NULL,
  `country` varchar(30) DEFAULT NULL,
  `email` text,
  `note` text,
  `phone` varchar(128) DEFAULT NULL,
  `street` varchar(128) DEFAULT NULL,
  `title` varchar(32) DEFAULT NULL,
  `zip` varchar(16) DEFAULT NULL,
  `sendMail` tinyint(1) NOT NULL DEFAULT '0',
  `organisation_ID` bigint(20) DEFAULT NULL,
  `activated` tinyint(1) DEFAULT NULL,
  `activation_md5` varchar(80) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  `hidden` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_collaborationuser_organisation1_idx` (`organisation_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=31 ;

--
-- Daten für Tabelle `collaborationuser`
--

INSERT INTO `collaborationuser` (`id`, `username`, `password`, `salt`, `name`, `firstName`, `city`, `country`, `email`, `note`, `phone`, `street`, `title`, `zip`, `sendMail`, `organisation_ID`, `activated`, `activation_md5`, `enabled`, `hidden`) VALUES
(11, 'urbauer', 'bc95a521f982971d9cea1dff744ce427', 'NTE303gN1u56', 'Urbauer', 'Philipp', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 1, 2, 1, NULL, 1, 0),
(16, 'sabutsch', 'f76e107980f8dcc3edfa350f9f5f4899', 'L7rQ1VJ31506', 'Sabutsch', 'Stefan', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 3, 1, NULL, 1, 0),
(17, 'seerainer', '096878fcd71084430a0af5586154971d', 'vh7Wy964B20D', 'Seerainer', 'Carina', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 4, 1, NULL, 1, 0),
(18, 'sebek', '87fc5d87b3c1386fb3e20d2446facceb', 's8fTKLmQfet6', 'Sebek', 'Walter', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 5, 1, NULL, 1, 0),
(19, 'kleyhons', 'ec19a1e42ec7757cabfd403ac85a9d59', 'HXdTf3nZ2P0y', 'Kleyhons', 'Rainer', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 6, 1, NULL, 1, 0),
(20, 'egger', 'a5249ec64ab262a0004a56a3f6975707', '3Rrv6A79l6Ys', 'Egger', 'Andreas', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 7, 1, NULL, 1, 0),
(21, 'pesec', 'cb42ad894a0e5f246768779b07c37155', 'S3WlTdf2ojXQ', 'Pesec', 'Bernhard', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 8, 1, NULL, 1, 0),
(22, 'waellisch', '3829d6cd5e947b2a60d4c2a5bcf61259', 'ffm3uFSl73uF', 'Wällisch', 'Diana', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 9, 1, NULL, 1, 0),
(23, 'brosch', 'bdbe9b16904bd41620c0517de1ada8d7', 'S3zUAdXhd5U1', 'Brosch', 'Peter', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 10, 1, NULL, 1, 0),
(24, 'stark', 'a1642c25988424f1e4565b2815c2beb2', 'mB3102AK6d7X', 'Stark', 'Günther', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 11, 1, NULL, 1, 0),
(26, 'brzTester', '640c041e3880e8f6809248be1a28cc3b', 'ry3jU2r1V62Q', 'Brz', 'Brz', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 12, 1, '', 1, 0),
(27, 'schwab', '9bb9620e368b7d8c55b2383716a663b4', 'pMmOyq906pn5', 'Schwab', 'Walter', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 0, 13, 1, '', 1, 0),
(28, 'dreschnig', '1a60093d5881b8e6c3bc60e3822277d3', '55Ck0nd6Lthz', 'Dreschnig', 'Arno', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 1, 14, 1, '', 1, 0),
(29, 'ruzicka', '5e4419c05d3382bc3b4228cdca35fda9', '4NqbB09BI8br', 'Ruzicka', 'Dietmar', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 1, 15, 1, '', 1, 0),
(30, 'prenner', 'b31aba31ad86c1e564519d69c278b61b', 'q7btA4Ufn055', 'Prenner', 'Engelbert', NULL, NULL, 'philipp.urbauer@technikum-wien.at', NULL, NULL, NULL, NULL, NULL, 1, 16, 1, '', 1, 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `discussion`
--

CREATE TABLE IF NOT EXISTS `discussion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lastDiscussionId` bigint(20) DEFAULT NULL,
  `date` timestamp NULL DEFAULT NULL,
  `changed` timestamp NULL DEFAULT NULL,
  `initial` tinyint(1) DEFAULT NULL,
  `longDescription` text,
  `shortDescription` text,
  `collaborationUserId` bigint(20) DEFAULT NULL,
  `proposalId` bigint(20) DEFAULT NULL,
  `proposalObjectId` bigint(20) DEFAULT NULL,
  `postNumber` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`),
  KEY `proposalID` (`proposalId`),
  KEY `proposalObjectID` (`proposalObjectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `discussiongroup`
--

CREATE TABLE IF NOT EXISTS `discussiongroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `head` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `discussiongroup2collaborationuser`
--

CREATE TABLE IF NOT EXISTS `discussiongroup2collaborationuser` (
  `discussionGroupId` bigint(20) NOT NULL,
  `collaborationuserId` bigint(20) NOT NULL,
  PRIMARY KEY (`discussionGroupId`,`collaborationuserId`),
  KEY `fk_discussionGroup_has_collaborationuser_collaborationuser1_idx` (`collaborationuserId`),
  KEY `fk_discussionGroup_has_collaborationuser_discussionGroup1_idx` (`discussionGroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `discussiongroupobject`
--

CREATE TABLE IF NOT EXISTS `discussiongroupobject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classname` text NOT NULL,
  `classId` bigint(20) NOT NULL,
  `discussionGroup_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_discussionGroupObject_discussionGroup1_idx` (`discussionGroup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `domain`
--

CREATE TABLE IF NOT EXISTS `domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `display_text` text COLLATE utf8_unicode_ci,
  `domain_oid` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `domain_codesys` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci,
  `domain_type` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `default_value_id` bigint(20) DEFAULT NULL,
  `display_order` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_domain_domain_value1_idx` (`default_value_id`),
  KEY `fk_domain_domain_value2_idx` (`display_order`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=92 ;

--
-- Daten für Tabelle `domain`
--

INSERT INTO `domain` (`id`, `name`, `display_text`, `domain_oid`, `domain_codesys`, `description`, `domain_type`, `default_value_id`, `display_order`) VALUES
(1, 'administrative_gender_cd', 'Geschlechter Codes', '2.16.840.1.113883.5.1', 'HL7', 'Geschlechter-Codes nach HL7-Norm', NULL, NULL, NULL),
(24, 'ISO 3166-1 alpha-3', 'Länderkürzel (3)', NULL, 'ISO', 'Länderkürzel (3 Buchstaben) nach ISO Norm', NULL, NULL, NULL),
(29, 'marital_status_cd', NULL, 'ophepa', NULL, NULL, NULL, NULL, NULL),
(42, 'PersonalRelationshipRoleType', NULL, '2.16.840.1.113883.5.111', 'HL7', NULL, NULL, NULL, NULL),
(60, 'validity_domain', NULL, NULL, NULL, NULL, NULL, NULL, 1311),
(61, 'Display Order', NULL, NULL, NULL, 'Domäne, wie Domänen sortiert werden können.', NULL, NULL, 1312),
(62, 'communication_types', NULL, NULL, NULL, 'Kommunikationstypen', NULL, NULL, NULL),
(63, 'ISO_639_1_Language_Codes', NULL, NULL, NULL, 'List of ISO 639-1 codes', NULL, NULL, 1312),
(69, 'mime_types', 'Mime Types', NULL, NULL, NULL, NULL, NULL, 1312),
(77, 'user_settings', 'Benutzer-Einstellungen', NULL, NULL, 'Alle Einträge dieser Domäne sind Einstellungen, welche einem Benutzer zugeordnet werden können', NULL, NULL, 1311),
(79, 'ISO-639-1', 'ISO-639-1 Sprachcodes', '', '', 'Beschreibung', '', NULL, 1312),
(80, 'Password_methods', 'Passwort Methoden', '', '', '', '', NULL, 1312),
(81, 'appsysobj_types', 'Anwendungsbausteine - Typen', '', '', '', '', NULL, 1312),
(85, 'task_priority', 'Priorität', NULL, NULL, '', NULL, NULL, 1311),
(86, 'task_status', NULL, NULL, NULL, NULL, NULL, 2189, 1311),
(87, 'attachment_technical_types', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(88, 'effort', NULL, NULL, NULL, NULL, NULL, 2217, 1311),
(89, 'ticket_types', NULL, NULL, NULL, NULL, NULL, NULL, 1312),
(90, 'ticket_status', NULL, NULL, NULL, NULL, NULL, NULL, 1311),
(91, 'ProposalTypes', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `domain_value`
--

CREATE TABLE IF NOT EXISTS `domain_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `domain_id` bigint(20) NOT NULL,
  `code` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `display_text` text COLLATE utf8_unicode_ci,
  `description` text COLLATE utf8_unicode_ci,
  `attrib1` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `attrib2` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `attrib3` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `order_no` int(11) DEFAULT NULL,
  `image_file` text COLLATE utf8_unicode_ci,
  `language_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `domain_fk` (`domain_id`),
  KEY `fk_domain_value_domain_value1_idx` (`language_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=2230 ;

--
-- Daten für Tabelle `domain_value`
--

INSERT INTO `domain_value` (`id`, `domain_id`, `code`, `display_text`, `description`, `attrib1`, `attrib2`, `attrib3`, `order_no`, `image_file`, `language_id`) VALUES
(1, 1, 'F', 'weiblich', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2, 1, 'M', 'männlich', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(4, 1, 'UN', 'unbekannt', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(642, 24, 'ABW', 'Aruba', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(643, 24, 'AFG', 'Afghanistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(644, 24, 'AGO', 'Angola', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(645, 24, 'AIA', 'Anguilla', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(646, 24, 'ALA', 'Åland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(647, 24, 'ALB', 'Albanien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(648, 24, 'AND', 'Andorra', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(649, 24, 'ANT', 'Niederländische Antillen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(650, 24, 'ARE', 'Vereinigte Arabische Emirate', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(651, 24, 'ARG', 'Argentinien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(652, 24, 'ARM', 'Armenien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(653, 24, 'ASC', 'Ascension (verwaltet von St. Helena, reserviert für UPU und ITU)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(654, 24, 'ASM', 'Amerikanisch-Samoa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(655, 24, 'ATA', 'Antarktis (Sonderstatus durch Antarktis-Vertrag)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(656, 24, 'ATF', 'Französische Süd- und Antarktisgebiete', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(657, 24, 'ATG', 'Antigua und Barbuda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(658, 24, 'AUS', 'Australien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(659, 24, 'AUT', 'Österreich', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(660, 24, 'AZE', 'Aserbaidschan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(661, 24, 'BDI', 'Burundi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(662, 24, 'BEL', 'Belgien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(663, 24, 'BEN', 'Benin', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(664, 24, 'BFA', 'Burkina Faso', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(665, 24, 'BGD', 'Bangladesch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(666, 24, 'BGR', 'Bulgarien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(667, 24, 'BHR', 'Bahrain', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(668, 24, 'BIH', 'Bosnien und Herzegowina', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(669, 24, 'BLM', 'Saint-Barthélemy', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(670, 24, 'BLR', 'Belarus (Weißrussland)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(671, 24, 'BLZ', 'Belize', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(672, 24, 'BMU', 'Bermuda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(673, 24, 'BOL', 'Bolivien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(674, 24, 'BRA', 'Brasilien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(675, 24, 'BRB', 'Barbados', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(676, 24, 'BRN', 'Brunei Darussalam', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(677, 24, 'BTN', 'Bhutan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(678, 24, 'BUR', 'Burma (jetzt Myanmar)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(679, 24, 'BVT', 'Bouvetinsel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(680, 24, 'BWA', 'Botsuana', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(681, 24, 'CAF', 'Zentralafrikanische Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(682, 24, 'CAN', 'Kanada', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(683, 24, 'CCK', 'Kokosinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(684, 24, 'CHE', 'Schweiz (Confoederatio Helvetica)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(685, 24, 'CHL', 'Chile', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(686, 24, 'CHN', 'China, Volksrepublik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(687, 24, 'CIV', 'Côte d''Ivoire (Elfenbeinküste)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(688, 24, 'CMR', 'Kamerun', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(689, 24, 'COD', 'Kongo, Demokratische Republik (ehem. Zaire)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(690, 24, 'COG', 'Republik Kongo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(691, 24, 'COK', 'Cookinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(692, 24, 'COL', 'Kolumbien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(693, 24, 'COM', 'Komoren', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(694, 24, 'CPT', 'Clipperton (reserviert für ITU)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(695, 24, 'CPV', 'Kap Verde', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(696, 24, 'CRI', 'Costa Rica', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(697, 24, 'CSK', 'Tschechoslowakei (ehemalig)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(698, 24, 'CUB', 'Kuba', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(699, 24, 'CXR', 'Weihnachtsinsel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(700, 24, 'CYM', 'Kaimaninseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(701, 24, 'CYP', 'Zypern', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(702, 24, 'CZE', 'Tschechische Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(703, 24, 'DEU', 'Deutschland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(704, 24, 'DGA', 'Diego Garcia (reserviert für ITU)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(705, 24, 'DJI', 'Dschibuti', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(706, 24, 'DMA', 'Dominica', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(707, 24, 'DNK', 'Dänemark', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(708, 24, 'DOM', 'Dominikanische Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(709, 24, 'DZA', 'Algerien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(710, 24, 'ECU', 'Ecuador', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(711, 24, 'EGY', 'Ägypten', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(712, 24, 'ERI', 'Eritrea', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(713, 24, 'ESH', 'Westsahara', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(714, 24, 'ESP', 'Spanien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(715, 24, 'EST', 'Estland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(716, 24, 'ETH', 'Äthiopien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(717, 24, 'FIN', 'Finnland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(718, 24, 'FJI', 'Fidschi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(719, 24, 'FLK', 'Falklandinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(720, 24, 'FRA', 'Frankreich', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(721, 24, 'FRO', 'Färöer', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(722, 24, 'FSM', 'Mikronesien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(723, 24, 'FXX', '(europ. Frankreich ohne Übersee-Départements)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(724, 24, 'GAB', 'Gabun', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(725, 24, 'GBR', 'Vereinigtes Königreich Großbritannien und Nordirland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(726, 24, 'GEO', 'Georgien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(727, 24, 'GGY', 'Guernsey (Kanalinsel)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(728, 24, 'GHA', 'Ghana', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(729, 24, 'GIB', 'Gibraltar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(730, 24, 'GIN', 'Guinea', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(731, 24, 'GLP', 'Guadeloupe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(732, 24, 'GMB', 'Gambia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(733, 24, 'GNB', 'Guinea-Bissau', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(734, 24, 'GNQ', 'Äquatorialguinea', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(735, 24, 'GRC', 'Griechenland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(736, 24, 'GRD', 'Grenada', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(737, 24, 'GRL', 'Grönland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(738, 24, 'GTM', 'Guatemala', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(739, 24, 'GUF', 'Französisch-Guayana', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(740, 24, 'GUM', 'Guam', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(741, 24, 'GUY', 'Guyana', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(742, 24, 'HKG', 'Hongkong', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(743, 24, 'HMD', 'Heard- und McDonald-Inseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(744, 24, 'HND', 'Honduras', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(745, 24, 'HRV', 'Kroatien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(746, 24, 'HTI', 'Haiti', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(747, 24, 'HUN', 'Ungarn', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(748, 24, 'IDN', 'Indonesien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(749, 24, 'IMN', 'Insel Man', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(750, 24, 'IND', 'Indien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(751, 24, 'IOT', 'Britisches Territorium im Indischen Ozean', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(752, 24, 'IRL', 'Irland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(753, 24, 'IRN', 'Iran, Islamische Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(754, 24, 'IRQ', 'Irak', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(755, 24, 'ISL', 'Island', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(756, 24, 'ISR', 'Israel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(757, 24, 'ITA', 'Italien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(758, 24, 'JAM', 'Jamaika', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(759, 24, 'JEY', 'Jersey (Kanalinsel)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(760, 24, 'JOR', 'Jordanien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(761, 24, 'JPN', 'Japan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(762, 24, 'KAZ', 'Kasachstan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(763, 24, 'KEN', 'Kenia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(764, 24, 'KGZ', 'Kirgisistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(765, 24, 'KHM', 'Kambodscha', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(766, 24, 'KIR', 'Kiribati', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(767, 24, 'KNA', 'St. Kitts und Nevis', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(768, 24, 'KOR', 'Korea, Republik (Südkorea)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(769, 24, 'KWT', 'Kuwait', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(770, 24, 'LAO', 'Laos, Demokratische Volksrepublik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(771, 24, 'LBN', 'Libanon', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(772, 24, 'LBR', 'Liberia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(773, 24, 'LBY', 'Libysch-Arabische Dschamahirija (Libyen)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(774, 24, 'LCA', 'St. Lucia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(775, 24, 'LIE', 'Liechtenstein', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(776, 24, 'LKA', 'Sri Lanka', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(777, 24, 'LSO', 'Lesotho', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(778, 24, 'LTU', 'Litauen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(779, 24, 'LUX', 'Luxemburg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(780, 24, 'LVA', 'Lettland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(781, 24, 'MAC', 'Macao', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(782, 24, 'MAF', 'Saint-Martin (franz. Teil)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(783, 24, 'MAR', 'Marokko', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(784, 24, 'MCO', 'Monaco', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(785, 24, 'MDA', 'Moldawien (Republik Moldau)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(786, 24, 'MDG', 'Madagaskar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(787, 24, 'MDV', 'Malediven', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(788, 24, 'MEX', 'Mexiko', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(789, 24, 'MHL', 'Marshallinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(790, 24, 'MKD', 'Mazedonien, ehem. jugoslawische Republik [2b]', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(791, 24, 'MLI', 'Mali', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(792, 24, 'MLT', 'Malta', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(793, 24, 'MMR', 'Myanmar (Burma)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(794, 24, 'MNE', 'Montenegro', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(795, 24, 'MNG', 'Mongolei', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(796, 24, 'MNP', 'Nördliche Marianen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(797, 24, 'MOZ', 'Mosambik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(798, 24, 'MRT', 'Mauretanien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(799, 24, 'MSR', 'Montserrat', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(800, 24, 'MTQ', 'Martinique', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(801, 24, 'MUS', 'Mauritius', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(802, 24, 'MWI', 'Malawi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(803, 24, 'MYS', 'Malaysia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(804, 24, 'MYT', 'Mayotte', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(805, 24, 'N', '', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(806, 24, 'NAM', 'Namibia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(807, 24, 'NCL', 'Neukaledonien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(808, 24, 'NER', 'Niger', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(809, 24, 'NFK', 'Norfolkinsel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(810, 24, 'NGA', 'Nigeria', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(811, 24, 'NIC', 'Nicaragua', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(812, 24, 'NIU', 'Niue', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(813, 24, 'NLD', 'Niederlande', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(814, 24, 'NOR', 'Norwegen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(815, 24, 'NPL', 'Nepal', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(816, 24, 'NRU', 'Nauru', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(817, 24, 'NTZ', 'Neutrale Zone (Saudi-Arabien und Irak)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(818, 24, 'NZL', 'Neuseeland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(819, 24, 'OMN', 'Oman', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(820, 24, 'PAK', 'Pakistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(821, 24, 'PAN', 'Panama', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(822, 24, 'PCN', 'Pitcairninseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(823, 24, 'PER', 'Peru', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(824, 24, 'PHL', 'Philippinen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(825, 24, 'PLW', 'Palau', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(826, 24, 'PNG', 'Papua-Neuguinea', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(827, 24, 'POL', 'Polen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(828, 24, 'PRI', 'Puerto Rico', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(829, 24, 'PRK', 'Korea, Demokratische Volksrepublik (Nordkorea)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(830, 24, 'PRT', 'Portugal', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(831, 24, 'PRY', 'Paraguay', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(832, 24, 'PSE', 'Palästinensische Autonomiegebiete', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(833, 24, 'PYF', 'Französisch-Polynesien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(834, 24, 'QAT', 'Katar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(835, 24, 'REU', 'Réunion', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(836, 24, 'ROU', 'Rumänien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(837, 24, 'RUS', 'Russische Föderation', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(838, 24, 'RWA', 'Ruanda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(839, 24, 'SAU', 'Saudi-Arabien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(840, 24, 'SCG', 'Serbien und Montenegro (ehemalig)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(841, 24, 'SDN', 'Sudan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(842, 24, 'SEN', 'Senegal', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(843, 24, 'SGP', 'Singapur', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(844, 24, 'SGS', 'Südgeorgien und die Südlichen Sandwichinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(845, 24, 'SHN', 'St. Helena', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(846, 24, 'SJM', 'Svalbard und Jan Mayen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(847, 24, 'SLB', 'Salomonen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(848, 24, 'SLE', 'Sierra Leone', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(849, 24, 'SLV', 'El Salvador', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(850, 24, 'SMR', 'San Marino', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(851, 24, 'SOM', 'Somalia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(852, 24, 'SPM', 'Saint-Pierre und Miquelon', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(853, 24, 'SRB', 'Serbien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(854, 24, 'STP', 'São Tomé und Príncipe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(855, 24, 'SUN', 'UdSSR (jetzt: Russische Föderation)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(856, 24, 'SUR', 'Suriname', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(857, 24, 'SVK', 'Slowakei', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(858, 24, 'SVN', 'Slowenien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(859, 24, 'SWE', 'Schweden', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(860, 24, 'SWZ', 'Swasiland', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(861, 24, 'SYC', 'Seychellen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(862, 24, 'SYR', 'Syrien, Arabische Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(863, 24, 'TAA', 'Tristan da Cunha (verwaltet von St. Helena, reserviert für UPU)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(864, 24, 'TCA', 'Turks- und Caicosinseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(865, 24, 'TCD', 'Tschad', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(866, 24, 'TGO', 'Togo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(867, 24, 'THA', 'Thailand', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(868, 24, 'TJK', 'Tadschikistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(869, 24, 'TKL', 'Tokelau', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(870, 24, 'TKM', 'Turkmenistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(871, 24, 'TLS', 'Osttimor (Timor-L''este)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(872, 24, 'TON', 'Tonga', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(873, 24, 'TTO', 'Trinidad und Tobago', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(874, 24, 'TUN', 'Tunesien', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(875, 24, 'TUR', 'Türkei', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(876, 24, 'TUV', 'Tuvalu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(877, 24, 'TWN', 'Republik China (Taiwan)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(878, 24, 'TZA', 'Tansania, Vereinigte Republik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(879, 24, 'UGA', 'Uganda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(880, 24, 'UKR', 'Ukraine', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(881, 24, 'UMI', 'United States Minor Outlying Islands', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(882, 24, 'URY', 'Uruguay', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(883, 24, 'USA', 'Vereinigte Staaten von Amerika', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(884, 24, 'UZB', 'Usbekistan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(885, 24, 'VAT', 'Vatikanstadt', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(886, 24, 'VCT', 'St. Vincent und die Grenadinen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(887, 24, 'VEN', 'Venezuela', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(888, 24, 'VGB', 'Britische Jungferninseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(889, 24, 'VIR', 'Amerikanische Jungferninseln', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(890, 24, 'VNM', 'Vietnam', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(891, 24, 'VUT', 'Vanuatu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(892, 24, 'WLF', 'Wallis und Futuna', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(893, 24, 'WSM', 'Samoa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(894, 24, 'YEM', 'Jemen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(895, 24, 'YUG', 'Jugoslawien (ehemalig)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(896, 24, 'ZAF', 'Südafrika', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(897, 24, 'ZAR', 'Zaire (jetzt Demokratische Republik Kongo)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(898, 24, 'ZMB', 'Sambia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(899, 24, 'ZWE', 'Simbabwe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(916, 29, 'D', 'geschieden', NULL, '6', NULL, NULL, NULL, NULL, 0),
(917, 29, 'k.A.', 'k.A.', NULL, '2', NULL, NULL, NULL, NULL, 0),
(918, 29, 'L', 'zusammen lebend', NULL, '5', NULL, NULL, NULL, NULL, 0),
(919, 29, 'M', 'verheiratet', NULL, '4', NULL, NULL, NULL, NULL, 0),
(920, 29, 'N', '', NULL, '1', NULL, NULL, NULL, NULL, 0),
(921, 29, 'S', 'ledig', NULL, '3', NULL, NULL, NULL, NULL, 0),
(922, 29, 'W', 'verwitwet', NULL, '7', NULL, NULL, NULL, NULL, 0),
(980, 42, 'AUNT', 'Tante', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(981, 42, 'BRO', 'Bruder', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(982, 42, 'BROINLAW', 'Bruder (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(983, 42, 'CHILD', 'Kind', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(984, 42, 'CHLDADOPT', 'Adoptivkind', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(985, 42, 'CHLDFOST', 'Pflegekind', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(986, 42, 'CHLDINLAW', 'Kind (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(987, 42, 'COUSN', 'Cousin/Cousine', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(988, 42, 'DAU', 'Tochter (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(989, 42, 'DAUADOPT', 'Adoptivtochter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(990, 42, 'DAUFOST', 'Pflegetochter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(991, 42, 'DAUINLAW', 'Tochter (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(992, 42, 'DOMPART', 'Mitbewohner', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(993, 42, 'FRND', 'Freund (nichtverwandt)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(994, 42, 'FTH', 'Vater', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(995, 42, 'FTHINLAW', 'Schwiegervater', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(996, 42, 'GGRFTH', 'Ur-Grossvater', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(997, 42, 'GGRMTH', 'Ur-Grossmutter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(998, 42, 'GGRPRN', 'Ur-Grosselternteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(999, 42, 'GRFTH', 'Grossvater', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1000, 42, 'GRMTH', 'Grossmutter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1001, 42, 'GRNDCHILD', 'Enkelkind', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1002, 42, 'GRNDDAU', 'Enkeltochter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1003, 42, 'GRNDSON', 'Enkelsohn', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1004, 42, 'GRPRN', 'Grosselternteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1005, 42, 'HBRO', 'Halbbruder', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1006, 42, 'HSIB', 'Halb-Geschwisterteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1007, 42, 'HSIS', 'Halbschwester', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1008, 42, 'HUSB', 'Ehemann', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1009, 42, 'MTH', 'Mutter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1010, 42, 'MTHINLOAW', 'Schwiegermutter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1011, 42, 'NBOR', 'Nachbar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1012, 42, 'NBRO', 'Bruder (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1013, 42, 'NCHILD', 'Kind (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1014, 42, 'NEPHEW', 'Neffe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1015, 42, 'NFTH', 'Vater (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1016, 42, 'NIECE', 'Nichte', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1017, 42, 'NIENEPH', 'Nichte/Neffe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1018, 42, 'NMTH', 'Mutter (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1019, 42, 'NONE', '', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1020, 42, 'NPRN', 'Elternteil (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1021, 42, 'NSIB', 'Geschwisterteil (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1022, 42, 'NSIS', 'Schwester (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1023, 42, 'PRN', 'Elternteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1024, 42, 'PRNINLAW', 'Schwiegereltern', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1025, 42, 'ROOM', 'Zimmergenosse', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1026, 42, 'SIB', 'Geschwisterteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1027, 42, 'SIBINLAW', 'Geschwisterteil (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1028, 42, 'SIGOTHR', 'Sonstiges', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1029, 42, 'SIS', 'Schwester', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1030, 42, 'SISLINLAW', 'Schwester (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1031, 42, 'SON', 'Sohn (natürlich)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1032, 42, 'SONADOPT', 'Adoptivsohn', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1033, 42, 'SONFOST', 'Pflegesohn', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1034, 42, 'SONINLAW', 'Sohn (angeheiratet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1035, 42, 'SPS', 'Ehepartner', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1036, 42, 'STPBRO', 'Stiefbruder', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1037, 42, 'STPCHLD', 'Stiefkind', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1038, 42, 'STPDAU', 'Stieftochter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1039, 42, 'STPFTH', 'Stiefvater', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1040, 42, 'STPMTH', 'Stiefmutter', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1041, 42, 'STPPRN', 'Stiefelternteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1042, 42, 'STPSIB', 'Stief-Geschwisterteil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1043, 42, 'STPSIS', 'Stiefschwester', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1044, 42, 'STPSON', 'Stiefsohn', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1045, 42, 'UNCLE', 'Onkel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1046, 42, 'WIFE', 'Ehefrau', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1310, 61, 'id', 'nach ID', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1311, 61, 'order', 'nach Order-Number', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1312, 61, 'name', 'nach Name', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1313, 60, 'system', 'System', NULL, NULL, NULL, NULL, 1, NULL, 0),
(1314, 60, 'modul', 'Modul', NULL, NULL, NULL, NULL, 2, NULL, 0),
(1315, 60, 'service', 'Service', NULL, NULL, NULL, NULL, 3, NULL, 0),
(1316, 60, 'usergroup', 'Benutzergruppe', NULL, NULL, NULL, NULL, 4, NULL, 0),
(1317, 60, 'user', 'Benutzer', NULL, NULL, NULL, NULL, 5, NULL, 0),
(1320, 62, 'tel', 'Telefon', NULL, NULL, NULL, NULL, NULL, '/rsc/img/communication/com_phone.png', 0),
(1321, 62, 'mobil', 'Handy', NULL, NULL, NULL, NULL, NULL, '/rsc/img/communication/com_handy.png', 0),
(1322, 62, 'mailto', 'Email', NULL, NULL, NULL, NULL, NULL, '/rsc/img/communication/com_email.png', 0),
(1323, 62, 'http', 'Webseite', NULL, NULL, NULL, NULL, NULL, '/rsc/img/communication/com_web.png', 0),
(1324, 62, 'ftp', 'File Transfer Protocol [RFC 1738]', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1325, 62, 'fax', 'Fax', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1326, 63, 'aa', 'Afar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1327, 63, 'ab', 'Abkhazian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1328, 63, 'ae', 'Avestan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1329, 63, 'af', 'Afrikaans', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1330, 63, 'ak', 'Akan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1331, 63, 'am', 'Amharic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1332, 63, 'an', 'Aragonese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1333, 63, 'ar', 'Arabic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1334, 63, 'as', 'Assamese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1335, 63, 'av', 'Avaric', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1336, 63, 'ay', 'Aymara', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1337, 63, 'az', 'Azerbaijani', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1338, 63, 'ba', 'Bashkir', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1339, 63, 'be', 'Belarusian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1340, 63, 'bg', 'Bulgarian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1341, 63, 'bh', 'Bihari', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1342, 63, 'bi', 'Bislama', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1343, 63, 'bm', 'Bambara', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1344, 63, 'bn', 'Bengali', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1345, 63, 'bo', 'Tibetan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1346, 63, 'br', 'Breton', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1347, 63, 'bs', 'Bosnian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1348, 63, 'ca', 'Catalan, Valencian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1349, 63, 'ce', 'Chechen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1350, 63, 'ch', 'Chamorro', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1351, 63, 'co', 'Corsican', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1352, 63, 'cr', 'Cree', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1353, 63, 'cs', 'Czech', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1354, 63, 'cu', 'Church Slavic, Old Slavonic, Church Slavonic, Old Bulgarian, Old Church Slavonic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1355, 63, 'cv', 'Chuvash', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1356, 63, 'cy', 'Welsh', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1357, 63, 'da', 'Danish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1358, 63, 'de', 'German', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1359, 63, 'dv', 'Divehi, Dhivehi, Maldivian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1360, 63, 'dz', 'Dzongkha', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1361, 63, 'ee', 'Ewe', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1362, 63, 'el', 'Modern Greek', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1363, 63, 'en', 'English', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1364, 63, 'eo', 'Esperanto', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1365, 63, 'es', 'Spanish, Castilian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1366, 63, 'et', 'Estonian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1367, 63, 'eu', 'Basque', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1368, 63, 'fa', 'Persian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1369, 63, 'ff', 'Fulah', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1370, 63, 'fi', 'Finnish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1371, 63, 'fj', 'Fijian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1372, 63, 'fo', 'Faroese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1373, 63, 'fr', 'French', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1374, 63, 'fy', 'Western Frisian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1375, 63, 'ga', 'Irish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1376, 63, 'gd', 'Gaelic, Scottish Gaelic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1377, 63, 'gl', 'Galician', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1378, 63, 'gn', 'Guaraní', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1379, 63, 'gu', 'Gujarati', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1380, 63, 'gv', 'Manx', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1381, 63, 'ha', 'Hausa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1382, 63, 'he', 'Modern Hebrew', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1383, 63, 'hi', 'Hindi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1384, 63, 'ho', 'Hiri Motu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1385, 63, 'hr', 'Croatian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1386, 63, 'ht', 'Haitian, Haitian Creole', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1387, 63, 'hu', 'Hungarian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1388, 63, 'hy', 'Armenian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1389, 63, 'hz', 'Herero', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1390, 63, 'ia', 'Interlingua (International Auxiliary Language Association)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1391, 63, 'id', 'Indonesian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1392, 63, 'ie', 'Interlingue, Occidental', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1393, 63, 'ig', 'Igbo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1394, 63, 'ii', 'Sichuan Yi, Nuosu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1395, 63, 'ik', 'Inupiaq', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1396, 63, 'io', 'Ido', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1397, 63, 'is', 'Icelandic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1398, 63, 'it', 'Italian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1399, 63, 'iu', 'Inuktitut', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1400, 63, 'ja', 'Japanese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1401, 63, 'jv', 'Javanese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1402, 63, 'ka', 'Georgian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1403, 63, 'kg', 'Kongo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1404, 63, 'ki', 'Kikuyu, Gikuyu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1405, 63, 'kj', 'Kwanyama, Kuanyama', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1406, 63, 'kk', 'Kazakh', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1407, 63, 'kl', 'Kalaallisut, Greenlandic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1408, 63, 'km', 'Central Khmer', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1409, 63, 'kn', 'Kannada', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1410, 63, 'ko', 'Korean', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1411, 63, 'kr', 'Kanuri', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1412, 63, 'ks', 'Kashmiri', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1413, 63, 'ku', 'Kurdish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1414, 63, 'kv', 'Komi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1415, 63, 'kw', 'Cornish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1416, 63, 'ky', 'Kirghiz, Kyrgyz', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1417, 63, 'la', 'Latin', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1418, 63, 'lb', 'Luxembourgish, Letzeburgesch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1419, 63, 'lg', 'Ganda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1420, 63, 'li', 'Limburgish, Limburgan, Limburger', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1421, 63, 'ln', 'Lingala', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1422, 63, 'lo', 'Lao', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1423, 63, 'lt', 'Lithuanian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1424, 63, 'lu', 'Luba-Katanga', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1425, 63, 'lv', 'Latvian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1426, 63, 'mg', 'Malagasy', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1427, 63, 'mh', 'Marshallese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1428, 63, 'mi', 'Maori', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1429, 63, 'mk', 'Macedonian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1430, 63, 'ml', 'Malayalam', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1431, 63, 'mn', 'Mongolian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1432, 63, 'mr', 'Marathi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1433, 63, 'ms', 'Malay', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1434, 63, 'mt', 'Maltese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1435, 63, 'my', 'Burmese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1436, 63, 'na', 'Nauru', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1437, 63, 'nb', 'Norwegian Bokmål', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1438, 63, 'nd', 'North Ndebele', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1439, 63, 'ne', 'Nepali', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1440, 63, 'ng', 'Ndonga', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1441, 63, 'nl', 'Dutch, Flemish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1442, 63, 'nn', 'Norwegian Nynorsk', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1443, 63, 'no', 'Norwegian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1444, 63, 'nr', 'South Ndebele', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1445, 63, 'nv', 'Navajo, Navaho', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1446, 63, 'ny', 'Chichewa, Chewa, Nyanja', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1447, 63, 'oc', 'Occitan (after 63500)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1448, 63, 'oj', 'Ojibwa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1449, 63, 'om', 'Oromo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1450, 63, 'or', 'Oriya', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1451, 63, 'os', 'Ossetian, Ossetic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1452, 63, 'pa', 'Panjabi, Punjabi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1453, 63, 'pi', 'Pali', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1454, 63, 'pl', 'Polish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1455, 63, 'ps', 'Pashto, Pushto', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1456, 63, 'pt', 'Portuguese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1457, 63, 'qu', 'Quechua', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1458, 63, 'rm', 'Romansh', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1459, 63, 'rn', 'Rundi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1460, 63, 'ro', 'Romanian, Moldavian, Moldovan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1461, 63, 'ru', 'Russian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1462, 63, 'rw', 'Kinyarwanda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1463, 63, 'sa', 'Sanskrit', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1464, 63, 'sc', 'Sardinian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1465, 63, 'sd', 'Sindhi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1466, 63, 'se', 'Northern Sami', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1467, 63, 'sg', 'Sango', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1468, 63, 'si', 'Sinhala, Sinhalese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1469, 63, 'sk', 'Slovak', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1470, 63, 'sl', 'Slovene', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1471, 63, 'sm', 'Samoan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1472, 63, 'sn', 'Shona', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1473, 63, 'so', 'Somali', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1474, 63, 'sq', 'Albanian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1475, 63, 'sr', 'Serbian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1476, 63, 'ss', 'Swati', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1477, 63, 'st', 'Southern Sotho', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1478, 63, 'su', 'Sundanese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1479, 63, 'sv', 'Swedish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1480, 63, 'sw', 'Swahili', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1481, 63, 'ta', 'Tamil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1482, 63, 'te', 'Telugu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1483, 63, 'tg', 'Tajik', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1484, 63, 'th', 'Thai', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1485, 63, 'ti', 'Tigrinya', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1486, 63, 'tk', 'Turkmen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1487, 63, 'tl', 'Tagalog', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1488, 63, 'tn', 'Tswana', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1489, 63, 'to', 'Tonga (Tonga Islands)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1490, 63, 'tr', 'Turkish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1491, 63, 'ts', 'Tsonga', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1492, 63, 'tt', 'Tatar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1493, 63, 'tw', 'Twi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1494, 63, 'ty', 'Tahitian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1495, 63, 'ug', 'Uighur, Uyghur', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1496, 63, 'uk', 'Ukrainian', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1497, 63, 'ur', 'Urdu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1498, 63, 'uz', 'Uzbek', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1499, 63, 've', 'Venda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1500, 63, 'vi', 'Vietnamese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1501, 63, 'vo', 'Volapük', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1502, 63, 'wa', 'Walloon', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1503, 63, 'wo', 'Wolof', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1504, 63, 'xh', 'Xhosa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1505, 63, 'yi', 'Yiddish', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1506, 63, 'yo', 'Yoruba', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1507, 63, 'za', 'Zhuang, Chuang', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1508, 63, 'zh', 'Chinese', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1509, 63, 'zu', 'Zulu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1571, 77, 'last_view', 'Letzte Ansicht', NULL, 'hidden', 'int', NULL, NULL, NULL, 0),
(1572, 77, 'last_class_id', 'Letzte Klassen-ID', NULL, 'hidden', 'long', NULL, NULL, NULL, 0),
(1573, 77, 'send_notification_mail', 'Sende Benachrichtigungs-Emails', NULL, 'visible', 'boolean', NULL, 1, NULL, 0),
(1574, 77, 'notification_mail_days', 'Alle x Tage', NULL, 'visible', 'int', NULL, 2, NULL, 0),
(1575, 77, 'save_last_view', 'Letzte Ansicht speichern', NULL, 'visible', 'boolean', NULL, NULL, NULL, 0),
(1576, 77, 'last_notification_checked_ts', 'Letztes Mal Benachrichtigungen geprüft', NULL, 'hidden', 'double', NULL, NULL, NULL, 0),
(1718, 79, 'aa', 'Afar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1719, 79, 'ab', 'Abchasisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1720, 79, 'af', 'Afrikaans', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1721, 79, 'am', 'Amharisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1722, 79, 'ar', 'Arabisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1723, 79, 'as', 'Assamesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1724, 79, 'ay', 'Aymara', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1725, 79, 'az', 'Aserbaidschanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1726, 79, 'ba', 'Baschkirisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1727, 79, 'be', 'Belorussisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1728, 79, 'bg', 'Bulgarisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1729, 79, 'bh', 'Biharisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1730, 79, 'bi', 'Bislamisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1731, 79, 'bn', 'Bengalisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1732, 79, 'bo', 'Tibetanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1733, 79, 'br', 'Bretonisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1734, 79, 'ca', 'Katalanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1735, 79, 'co', 'Korsisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1736, 79, 'cs', 'Tschechisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1737, 79, 'cy', 'Walisisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1738, 79, 'da', 'Dänisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1739, 79, 'de', 'Deutsch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1740, 79, 'dz', 'Dzongkha, Bhutani', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1741, 79, 'el', 'Griechisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1742, 79, 'en', 'Englisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1743, 79, 'eo', 'Esperanto', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1744, 79, 'es', 'Spanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1745, 79, 'et', 'Estnisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1746, 79, 'eu', 'Baskisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1747, 79, 'fa', 'Persisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1748, 79, 'fi', 'Finnisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1749, 79, 'fj', 'Fiji', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1750, 79, 'fo', 'Faröisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1751, 79, 'fr', 'Französisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1752, 79, 'fy', 'Friesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1753, 79, 'ga', 'Irisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1754, 79, 'gd', 'Schottisches Gälisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1755, 79, 'gl', 'Galizisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1756, 79, 'gn', 'Guarani', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1757, 79, 'gu', 'Gujaratisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1758, 79, 'ha', 'Haussa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1759, 79, 'he', 'Hebräisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1760, 79, 'hi', 'Hindi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1761, 79, 'hr', 'Kroatisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1762, 79, 'hu', 'Ungarisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1763, 79, 'hy', 'Armenisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1764, 79, 'ia', 'Interlingua', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1765, 79, 'id', 'Indonesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1766, 79, 'ie', 'Interlingue', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1767, 79, 'ik', 'Inupiak', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1768, 79, 'is', 'Isländisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1769, 79, 'it', 'Italienisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1770, 79, 'iu', 'Inuktitut', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1771, 79, 'iw', 'Hebräisch (veraltet, nun: he)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1772, 79, 'ja', 'Japanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1773, 79, 'ji', 'Jiddish (veraltet, nun: yi)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1774, 79, 'jv', 'Javanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1775, 79, 'ka', 'Georgisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1776, 79, 'kk', 'Kasachisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1777, 79, 'kl', 'Kalaallisut (Grönland)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1778, 79, 'km', 'Kambodschanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1779, 79, 'kn', 'Kannada', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1780, 79, 'ko', 'Koreanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1781, 79, 'ks', 'Kaschmirisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1782, 79, 'ku', 'Kurdisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1783, 79, 'ky', 'Kirgisisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1784, 79, 'la', 'Lateinisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1785, 79, 'ln', 'Lingala', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1786, 79, 'lo', 'Laotisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1787, 79, 'lt', 'Litauisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1788, 79, 'lv', 'Lettisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1789, 79, 'mg', 'Malagasisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1790, 79, 'mi', 'Maorisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1791, 79, 'mk', 'Mazedonisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1792, 79, 'ml', 'Malajalam', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1793, 79, 'mn', 'Mongolisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1794, 79, 'mo', 'Moldavisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1795, 79, 'mr', 'Marathi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1796, 79, 'ms', 'Malaysisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1797, 79, 'mt', 'Maltesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1798, 79, 'my', 'Burmesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1799, 79, 'na', 'Nauruisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1800, 79, 'ne', 'Nepalisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1801, 79, 'nl', 'Holländisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1802, 79, 'no', 'Norwegisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1803, 79, 'oc', 'Okzitanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1804, 79, 'om', 'Oromo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1805, 79, 'or', 'Orija', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1806, 79, 'pa', 'Pundjabisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1807, 79, 'pl', 'Polnisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1808, 79, 'ps', 'Paschtu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1809, 79, 'pt', 'Portugiesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1810, 79, 'qu', 'Quechua', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1811, 79, 'rm', 'Rätoromanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1812, 79, 'rn', 'Kirundisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1813, 79, 'ro', 'Rumänisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1814, 79, 'ru', 'Russisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1815, 79, 'rw', 'Kijarwanda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1816, 79, 'sa', 'Sanskrit', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1817, 79, 'sd', 'Zinti', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1818, 79, 'sg', 'Sango', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1819, 79, 'sh', 'Serbokroatisch (veraltet)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1820, 79, 'si', 'Singhalesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1821, 79, 'sk', 'Slowakisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1822, 79, 'sl', 'Slowenisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1823, 79, 'sm', 'Samoanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1824, 79, 'sn', 'Schonisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1825, 79, 'so', 'Somalisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1826, 79, 'sq', 'Albanisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1827, 79, 'sr', 'Serbisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1828, 79, 'ss', 'Swasiländisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1829, 79, 'st', 'Sesothisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1830, 79, 'su', 'Sudanesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1831, 79, 'sv', 'Schwedisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1832, 79, 'sw', 'Suaheli', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1833, 79, 'ta', 'Tamilisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1834, 79, 'te', 'Tegulu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1835, 79, 'tg', 'Tadschikisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1836, 79, 'th', 'Thai', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1837, 79, 'ti', 'Tigrinja', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1838, 79, 'tk', 'Turkmenisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1839, 79, 'tl', 'Tagalog', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1840, 79, 'tn', 'Sezuan', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1841, 79, 'to', 'Tongaisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1842, 79, 'tr', 'Türkisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1843, 79, 'ts', 'Tsongaisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1844, 79, 'tt', 'Tatarisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1845, 79, 'tw', 'Twi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1846, 79, 'ug', 'Uigur', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1847, 79, 'uk', 'Ukrainisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1848, 79, 'ur', 'Urdu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1849, 79, 'uz', 'Usbekisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1850, 79, 'vi', 'Vietnamesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1851, 79, 'vo', 'Volapük', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1852, 79, 'wo', 'Wolof', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1853, 79, 'xh', 'Xhosa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1854, 79, 'yi', 'Jiddish (früher: ji)', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1855, 79, 'yo', 'Joruba', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1856, 79, 'za', 'Zhuang', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1857, 79, 'zh', 'Chinesisch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1858, 79, 'zu', 'Zulu', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1859, 69, '.323', 'text/h323', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1860, 69, '.aac', 'audio/aac', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1861, 69, '.abw', 'application/abiword', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1862, 69, '.acx', 'application/internet-property-stream', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1863, 69, '.ai', 'application/illustrator', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1864, 69, '.aif', 'audio/aiff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1865, 69, '.aifc', 'audio/aiff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1866, 69, '.aiff', 'audio/aiff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1867, 69, '.asf', 'video/x-ms-asf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1868, 69, '.asp', 'application/x-asp', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1869, 69, '.asr', 'video/x-ms-asf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1870, 69, '.asx', 'video/x-ms-asf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1871, 69, '.au', 'audio/basic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1872, 69, '.avi', 'video/avi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1873, 69, '.axs', 'application/olescript', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1874, 69, '.bas', 'text/plain', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1875, 69, '.bin', 'application/octet-stream', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1876, 69, '.bmp', 'image/bmp', NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `domain_value` (`id`, `domain_id`, `code`, `display_text`, `description`, `attrib1`, `attrib2`, `attrib3`, `order_no`, `image_file`, `language_id`) VALUES
(1877, 69, '.bz2', 'application/x-bzip2', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1878, 69, '.c', 'text/x-csrc', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1879, 69, '.c++', 'text/x-c++src', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1880, 69, '.cab', 'application/vnd.ms-cab-compressed', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1881, 69, '.cat', 'application/vnd.ms-pkiseccat', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1882, 69, '.cct', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1883, 69, '.cdf', 'application/cdf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1884, 69, '.cer', 'application/x-x509-ca-cert', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1885, 69, '.cfc', 'application/x-cfm', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1886, 69, '.cfm', 'application/x-cfm', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1887, 69, '.class', 'application/x-java', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1888, 69, '.clp', 'application/x-msclip', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1889, 69, '.cmx', 'image/x-cmx', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1890, 69, '.cod', 'image/cis-cod', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1891, 69, '.cp', 'text/x-c++src', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1892, 69, '.cpio', 'application/x-cpio', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1893, 69, '.cpp', 'text/x-c++src', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1894, 69, '.crd', 'application/x-mscardfile', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1895, 69, '.crt', 'application/x-x509-ca-cert', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1896, 69, '.crl', 'application/pkix-crl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1897, 69, '.crt', 'application/x-x509-ca-cert', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1898, 69, '.csh', 'application/x-csh', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1899, 69, '.css', 'text/css', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1900, 69, '.cst', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1901, 69, '.csv', 'text/csv', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1902, 69, '.cxt', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1903, 69, '.dcr', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1904, 69, '.der', 'application/x-x509-ca-cert', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1905, 69, '.dib', 'image/bmp', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1906, 69, '.diff', 'text/x-patch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1907, 69, '.dir', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1908, 69, '.dll', 'application/x-msdownload', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1909, 69, '.dms', 'application/octet-stream', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1910, 69, '.doc', 'application/vnd.ms-word', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1911, 69, '.docm', 'application/vnd.ms-word.document.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1912, 69, '.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1913, 69, '.dot', 'application/msword', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1914, 69, '.dotm', 'application/vnd.ms-word.template.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1915, 69, '.dotx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.template', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1916, 69, '.dta', 'application/x-stata', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1917, 69, '.dv', 'video/x-dv', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1918, 69, '.dvi', 'application/x-dvi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1919, 69, '.dwg', 'image/x-dwg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1920, 69, '.dxf', 'application/x-autocad', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1921, 69, '.dxr', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1922, 69, '.elc', 'application/x-elc', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1923, 69, '.eml', 'message/rfc822', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1924, 69, '.enl', 'application/x-endnote-library', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1925, 69, '.enz', 'application/x-endnote-library', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1926, 69, '.eps', 'application/postscript', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1927, 69, '.etx', 'text/x-setext', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1928, 69, '.evy', 'application/envoy', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1929, 69, '.exe', 'application/x-msdos-program', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1930, 69, '.fif', 'application/fractals', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1931, 69, '.flr', 'x-world/x-vrml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1932, 69, '.fm', 'application/vnd.framemaker', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1933, 69, '.fqd', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1934, 69, '.gif', 'image/gif', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1935, 69, '.gtar', 'application/tar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1936, 69, '.gz', 'application/gzip', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1937, 69, '.h', 'text/x-chdr', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1938, 69, '.hdf', 'application/x-hdf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1939, 69, '.hlp', 'application/winhlp', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1940, 69, '.hqx', 'application/binhex', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1941, 69, '.hta', 'application/hta', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1942, 69, '.htc', 'text/x-component', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1943, 69, '.htm', 'text/html', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1944, 69, '.html', 'text/html', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1945, 69, '.htt', 'text/webviewhtml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1946, 69, '.ico', 'image/x-ico', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1947, 69, '.ics', 'text/calendar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1948, 69, '.ief', 'image/ief', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1949, 69, '.iii', 'application/x-iphone', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1950, 69, '.indd', 'application/x-indesign', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1951, 69, '.ins', 'application/x-internet-signup', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1952, 69, '.isp', 'application/x-internet-signup', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1953, 69, '.jar', 'application/java-archive', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1954, 69, '.java', 'text/x-java', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1955, 69, '.jfif', 'image/jpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1956, 69, '.jpe', 'image/jpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1957, 69, '.jpeg', 'image/jpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1958, 69, '.jpg', 'image/jpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1959, 69, '.js', 'text/javascript', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1960, 69, '.kml', 'application/vnd.google-earth.kml+xml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1961, 69, '.kmz', 'application/vnd.google-earth.kmz', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1962, 69, '.latex', 'application/x-latex', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1963, 69, '.lha', 'application/x-lha', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1964, 69, '.lib', 'application/x-endnote-library', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1965, 69, '.llb', 'application/x-labview', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1966, 69, '.log', 'text/x-log', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1967, 69, '.lsf', 'video/x-la-asf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1968, 69, '.lsx', 'video/x-la-asf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1969, 69, '.lvx', 'application/x-labview-exec', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1970, 69, '.lzh', 'application/x-lha', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1971, 69, '.m', 'text/x-objcsrc', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1972, 69, '.m1v', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1973, 69, '.m2v', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1974, 69, '.m3u', 'audio/x-mpegurl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1975, 69, '.m4a', 'audio/m4a', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1976, 69, '.m4v', 'video/mp4', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1977, 69, '.ma', 'application/mathematica', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1978, 69, '.mail', 'message/rfc822', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1979, 69, '.man', 'application/x-troff-man', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1980, 69, '.mcd', 'application/x-mathcad', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1981, 69, '.mdb', 'application/vnd.ms-access', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1982, 69, '.me', 'application/x-troff-me', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1983, 69, '.mfp', 'application/x-shockwave-flash', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1984, 69, '.mht', 'message/rfc822', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1985, 69, '.mhtml', 'message/rfc822', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1986, 69, '.mid', 'audio/x-midi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1987, 69, '.midi', 'audio/x-midi', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1988, 69, '.mif', 'application/vnd.framemaker', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1989, 69, '.mny', 'application/x-msmoney', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1990, 69, '.mov', 'video/quicktime', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1991, 69, '.mp2', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1992, 69, '.mp3', 'audio/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1993, 69, '.mpa', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1994, 69, '.mpe', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1995, 69, '.mpeg', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1996, 69, '.mpg', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1997, 69, '.mpp', 'application/vnd.ms-project', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1998, 69, '.mpv2', 'video/mpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1999, 69, '.mqv', 'video/quicktime', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2000, 69, '.ms', 'application/x-troff-ms', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2001, 69, '.mvb', 'application/x-msmediaview', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2002, 69, '.mws', 'application/x-maple', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2003, 69, '.nb', 'application/mathematica', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2004, 69, '.nws', 'message/rfc822', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2005, 69, '.oda', 'application/oda', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2006, 69, '.odf', 'application/vnd.oasis.opendocument.formula', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2007, 69, '.odg', 'application/vnd.oasis.opendocument.graphics', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2008, 69, '.odp', 'application/vnd.oasis.opendocument.presentation', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2009, 69, '.ods', 'application/vnd.oasis.opendocument.spreadsheet', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2010, 69, '.odt', 'application/vnd.oasis.opendocument.text', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2011, 69, '.ogg', 'application/ogg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2012, 69, '.one', 'application/msonenote', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2013, 69, '.p12', 'application/x-x509-ca-cert', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2014, 69, '.patch', 'text/x-patch', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2015, 69, '.pbm', 'image/x-portable-bitmap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2016, 69, '.pcd', 'image/x-photo-cd', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2017, 69, '.pct', 'image/x-pict', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2018, 69, '.pdf', 'application/pdf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2019, 69, '.pfx', 'application/x-pkcs12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2020, 69, '.pgm', 'image/x-portable-graymap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2021, 69, '.php', 'application/x-php', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2022, 69, '.pic', 'image/x-pict', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2023, 69, '.pict', 'image/x-pict', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2024, 69, '.pjpeg', 'image/jpeg', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2025, 69, '.pl', 'application/x-perl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2026, 69, '.pls', 'audio/x-mpegurl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2027, 69, '.pko', 'application/ynd.ms-pkipko', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2028, 69, '.pm', 'application/x-perl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2029, 69, '.pmc', 'application/x-perfmon', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2030, 69, '.png', 'image/png', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2031, 69, '.pnm', 'image/x-portable-anymap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2032, 69, '.pod', 'text/x-pod', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2033, 69, '.potm', 'application/vnd.ms-powerpoint.template.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2034, 69, '.potx', 'application/vnd.openxmlformats-officedocument.presentationml.template', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2035, 69, '.ppam', 'application/vnd.ms-powerpoint.addin.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2036, 69, '.ppm', 'image/x-portable-pixmap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2037, 69, '.pps', 'application/vnd.ms-powerpoint', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2038, 69, '.ppsm', 'application/vnd.ms-powerpoint.slideshow.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2039, 69, '.ppsx', 'application/vnd.openxmlformats-officedocument.presentationml.slideshow', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2040, 69, '.ppt', 'application/vnd.ms-powerpoint', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2041, 69, '.pptm', 'application/vnd.ms-powerpoint.presentation.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2042, 69, '.pptx', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2043, 69, '.prf', 'application/pics-rules', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2044, 69, '.ps', 'application/postscript', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2045, 69, '.psd', 'application/photoshop', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2046, 69, '.pub', 'application/vnd.ms-publisher', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2047, 69, '.py', 'text/x-python', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2048, 69, '.qt', 'video/quicktime', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2049, 69, '.ra', 'audio/vnd.rn-realaudio', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2050, 69, '.ram', 'audio/vnd.rn-realaudio', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2051, 69, '.rar', 'application/rar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2052, 69, '.ras', 'image/x-cmu-raster', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2053, 69, '.rgb', 'image/x-rgb', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2054, 69, '.rm', 'application/vnd.rn-realmedia', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2055, 69, '.rmi', 'audio/mid', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2056, 69, '.roff', 'application/x-troff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2057, 69, '.rpm', 'audio/vnd.rn-realaudio', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2058, 69, '.rtf', 'application/rtf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2059, 69, '.rtx', 'application/rtf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2060, 69, '.rv', 'video/vnd.rn-realvideo', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2061, 69, '.sas', 'application/sas', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2062, 69, '.sav', 'application/spss', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2063, 69, '.scd', 'application/x-msschedule', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2064, 69, '.scm', 'text/x-script.scheme', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2065, 69, '.sct', 'text/scriptlet', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2066, 69, '.sd2', 'application/spss', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2067, 69, '.sea', 'application/x-sea', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2068, 69, '.sh', 'application/x-sh', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2069, 69, '.shar', 'application/x-shar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2070, 69, '.shtml', 'text/html', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2071, 69, '.sit', 'application/stuffit', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2072, 69, '.smil', 'application/smil', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2073, 69, '.snd', 'audio/basic', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2074, 69, '.spl', 'application/x-shockwave-flash', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2075, 69, '.spo', 'application/spss', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2076, 69, '.sql', 'text/x-sql', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2077, 69, '.src', 'application/x-wais-source', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2078, 69, '.sst', 'application/vnd.ms-pkicertstore', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2079, 69, '.stl', 'application/vnd.ms-pkistl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2080, 69, '.stm', 'text/html', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2081, 69, '.swa', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2082, 69, '.swf', 'application/x-shockwave-flash', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2083, 69, '.sxw', 'application/vnd.sun.xml.writer', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2084, 69, '.t', 'application/x-troff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2085, 69, '.tar', 'application/tar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2086, 69, '.tcl', 'application/x-tcl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2087, 69, '.tex', 'application/x-tex', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2088, 69, '.tga', 'image/x-targa', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2089, 69, '.tgz', 'application/gzip', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2090, 69, '.tif', 'image/tiff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2091, 69, '.tiff', 'image/tiff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2092, 69, '.tnef', 'application/ms-tnef', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2093, 69, '.tr', 'application/x-troff', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2094, 69, '.trm', 'application/x-msterminal', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2095, 69, '.tsv', 'text/tsv', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2096, 69, '.twb', 'application/twb', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2097, 69, '.twbx', 'application/twb', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2098, 69, '.txt', 'text/plain', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2099, 69, '.uls', 'text/iuls', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2100, 69, '.ustar', 'application/x-ustar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2101, 69, '.vcf', 'text/x-vcard', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2102, 69, '.vrml', 'x-world/x-vrml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2103, 69, '.vsd', 'application/vnd.visio', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2104, 69, '.w3d', 'application/x-director', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2105, 69, '.war', 'application/x-webarchive', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2106, 69, '.wav', 'audio/wav', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2107, 69, '.wcm', 'application/vnd.ms-works', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2108, 69, '.wdb', 'application/vnd.ms-works', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2109, 69, '.wks', 'application/vnd.ms-works', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2110, 69, '.wma', 'audio/x-ms-wma', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2111, 69, '.wmf', 'image/x-wmf', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2112, 69, '.wmv', 'video/x-ms-wmv', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2113, 69, '.wmz', 'application/x-ms-wmz', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2114, 69, '.wpd', 'application/wordperfect', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2115, 69, '.wps', 'application/vnd.ms-works', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2116, 69, '.wri', 'application/x-mswrite', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2117, 69, '.wrl', 'x-world/x-vrml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2118, 69, '.wrz', 'x-world/x-vrml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2119, 69, '.xbm', 'image/x-xbitmap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2120, 69, '.xhtml', 'text/html', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2121, 69, '.xla', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2122, 69, '.xlam', 'application/vnd.ms-excel.addin.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2123, 69, '.xlc', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2124, 69, '.xll', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2125, 69, '.xlm', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2126, 69, '.xls', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2127, 69, '.xlsb', 'application/vnd.ms-excel.sheet.binary.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2128, 69, '.xlsm', 'application/vnd.ms-excel.sheet.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2129, 69, '.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2130, 69, '.xlt', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2131, 69, '.xltm', 'application/vnd.ms-excel.template.macroEnabled.12', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2132, 69, '.xltx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.template', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2133, 69, '.xlw', 'application/vnd.ms-excel', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2134, 69, '.xml', 'text/xml', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2135, 69, '.xpm', 'image/x-xpixmap', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2136, 69, '.xps', 'application/vnd.ms-xpsdocument', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2137, 69, '.xsl', 'text/xsl', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2138, 69, '.xwd', 'image/x-xwindowdump', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2139, 69, '.z', 'application/x-compress', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2140, 69, '.zip', 'application/zip', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2141, 80, 'md5_salt_1000', 'MD5 mit Salt und 1000 Iterationen', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2142, 80, 'md5', 'MD5', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2143, 80, 'md5_salt', 'MD5 mit Salt', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2148, 77, 'stddetails_maximized', 'Zeige Standard-Details maximiert', NULL, 'hidden', 'boolean', NULL, NULL, NULL, 0),
(2149, 77, 'last_project_id', 'Letzte Projekt-ID', NULL, 'hidden', 'long', NULL, NULL, NULL, 0),
(2151, 81, 'menu', 'Hauptmenü', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2152, 81, 'submenu', 'Untermenü', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2153, 81, 'toolmenu', 'Toolbar', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2154, 81, 'param', 'Parametrierung', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2184, 85, '-1', 'keine', NULL, NULL, NULL, NULL, -1, NULL, 0),
(2185, 85, '0', 'niedrig', NULL, NULL, NULL, NULL, 0, NULL, 0),
(2186, 85, '1', 'mittel', NULL, NULL, NULL, NULL, 1, NULL, 0),
(2187, 85, '2', 'hoch', NULL, NULL, NULL, NULL, 2, NULL, 0),
(2188, 85, '3', 'top', NULL, 'color:#CC6600; font-weight:bold;', NULL, NULL, 3, NULL, 0),
(2189, 86, 'none', '-', NULL, NULL, NULL, NULL, 0, NULL, 0),
(2190, 86, 'active', 'aktiv', NULL, NULL, NULL, NULL, 1, NULL, 0),
(2191, 86, 'planing', 'geplant', NULL, NULL, NULL, NULL, 2, NULL, 0),
(2192, 86, 'canceled', 'abgebrochen', NULL, NULL, NULL, NULL, 3, NULL, 0),
(2193, 86, 'holding', 'unterbrochen', NULL, NULL, NULL, NULL, 4, NULL, 0),
(2194, 86, 'delegated', 'delegiert', NULL, NULL, NULL, NULL, 5, NULL, 0),
(2195, 86, 'completed', 'erledigt', NULL, NULL, NULL, NULL, 6, NULL, 0),
(2196, 87, '1', 'Dokument', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2197, 87, '2', 'Link', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2198, 87, '3', 'Notiz', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2199, 88, '15min', '15 Min', NULL, '0.25', NULL, NULL, 10, NULL, 0),
(2200, 88, '30min', '30 Min', NULL, '0.5', NULL, NULL, 20, NULL, 0),
(2201, 88, '1hour', '1 Std', NULL, '1', NULL, NULL, 30, NULL, 0),
(2202, 88, '1halfhour', '1,5 Std', NULL, '1.5', NULL, NULL, 40, NULL, 0),
(2203, 88, '2hours', '2 Std', NULL, '2', NULL, NULL, 50, NULL, 0),
(2204, 88, '3hours', '3 Std', NULL, '3', NULL, NULL, 60, NULL, 0),
(2205, 88, '4hours', '4 Std', NULL, '4', NULL, NULL, 70, NULL, 0),
(2206, 88, '6hours', '6 Std', NULL, '6', NULL, NULL, 80, NULL, 0),
(2207, 88, '1day', '1 Tag', NULL, '8', NULL, NULL, 90, NULL, 0),
(2208, 88, '2days', '2 Tage', NULL, '16', NULL, NULL, 100, NULL, 0),
(2209, 88, '3days', '3 Tage', NULL, '24', NULL, NULL, 110, NULL, 0),
(2210, 88, '4days', '4 Tage', NULL, '32', NULL, NULL, 120, NULL, 0),
(2211, 88, '1week', '1 Woche', NULL, '40', NULL, NULL, 130, NULL, 0),
(2212, 88, '2weeks', '2 Wochen', NULL, '80', NULL, NULL, 140, NULL, 0),
(2213, 88, '3weeks', '3 Wochen', NULL, '120', NULL, NULL, 150, NULL, 0),
(2214, 88, '1month', '1 Monat', NULL, '160', NULL, NULL, 160, NULL, 0),
(2215, 88, '2months', '2 Monate', NULL, '320', NULL, NULL, 170, NULL, 0),
(2216, 88, 'greater2months', '> 2 Monate', NULL, '1000', NULL, NULL, 180, NULL, 0),
(2217, 88, 'none', '-', NULL, '0', NULL, NULL, 0, NULL, 0),
(2218, 89, 'failure', 'Fehler', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2219, 89, 'question', 'Frage', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2220, 89, 'proposal', 'Vorschlag', NULL, NULL, NULL, NULL, NULL, NULL, 0),
(2221, 90, 'new', 'neu', NULL, NULL, NULL, NULL, 1, NULL, 0),
(2222, 90, 'opened', 'geöffnet', NULL, NULL, NULL, NULL, 2, NULL, 0),
(2223, 90, 'closed', 'geschlossen', NULL, NULL, NULL, NULL, 4, NULL, 0),
(2224, 90, 'reopened', 'erneut geöffnet', NULL, NULL, NULL, NULL, 3, NULL, 0),
(2225, 91, 'concept', 'Begriff', NULL, NULL, NULL, NULL, NULL, NULL, 1358),
(2226, 91, 'vocabulary', 'Code System', NULL, NULL, NULL, NULL, NULL, NULL, 1358),
(2227, 91, 'subconcept', 'Unterkonzept', NULL, NULL, NULL, NULL, NULL, NULL, 1358),
(2228, 91, 'conceptVs', 'Konzept Value Set Zugehörigkeit', NULL, NULL, NULL, NULL, NULL, NULL, 1358),
(2229, 91, 'valueset', 'Value Set', NULL, NULL, NULL, NULL, NULL, NULL, 1358);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `domainvalue2domainvalue`
--

CREATE TABLE IF NOT EXISTS `domainvalue2domainvalue` (
  `domain_value_id_parent` bigint(20) NOT NULL,
  `domain_value_id_child` bigint(20) NOT NULL,
  PRIMARY KEY (`domain_value_id_parent`,`domain_value_id_child`),
  KEY `fk_domainvalue2domainvalue_domain_value1` (`domain_value_id_parent`),
  KEY `fk_domainvalue2domainvalue_domain_value2` (`domain_value_id_child`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `enquiry`
--

CREATE TABLE IF NOT EXISTS `enquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `collaborationUserId` bigint(20) NOT NULL,
  `requestType` varchar(100) NOT NULL,
  `requestDescription` text NOT NULL,
  `termName` varchar(100) DEFAULT NULL,
  `termDescription` text,
  `intendedValidityRange` varchar(100) DEFAULT NULL,
  `extPerson` bigint(20) DEFAULT NULL,
  `closedFlag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `extPerson` (`extPerson`),
  KEY `collaborationUserId` (`collaborationUserId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `file`
--

CREATE TABLE IF NOT EXISTS `file` (
  `link_id` bigint(20) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`link_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `link`
--

CREATE TABLE IF NOT EXISTS `link` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text,
  `insert_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `description` text,
  `linkType` int(11) DEFAULT NULL,
  `collaborationUserId` bigint(20) NOT NULL,
  `discussionId` bigint(20) DEFAULT NULL,
  `proposalId` bigint(20) DEFAULT NULL,
  `mimeType` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`),
  KEY `discussionID` (`discussionId`),
  KEY `proposalID` (`proposalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `organisation`
--

CREATE TABLE IF NOT EXISTS `organisation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `organisation` text NOT NULL,
  `organisationAbbr` text,
  `organisationLink` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=17 ;

--
-- Daten für Tabelle `organisation`
--

INSERT INTO `organisation` (`id`, `organisation`, `organisationAbbr`, `organisationLink`) VALUES
(2, 'FH Technikum Wien', 'FHTW', NULL),
(3, 'ELGA Gmbh', NULL, NULL),
(4, 'ELGA Gmbh', NULL, NULL),
(5, 'BMG', NULL, NULL),
(6, 'BMG', NULL, NULL),
(7, 'BMG', NULL, NULL),
(8, 'DotHealth', NULL, NULL),
(9, 'BRZ', NULL, NULL),
(10, 'BMG', NULL, NULL),
(11, 'Stark', NULL, NULL),
(12, 'Brz', NULL, NULL),
(13, 'Kages', NULL, NULL),
(14, 'CAS', NULL, NULL),
(15, 'CAS', NULL, NULL),
(16, 'BMG', NULL, NULL);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `privilege`
--

CREATE TABLE IF NOT EXISTS `privilege` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proposalId` bigint(20) DEFAULT NULL,
  `collaborationUserId` bigint(20) DEFAULT NULL,
  `fromDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mayChangeStatus` tinyint(1) DEFAULT NULL,
  `mayManageObjects` tinyint(1) DEFAULT NULL,
  `sendMail` tinyint(1) DEFAULT NULL,
  `discussionGroupId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`),
  KEY `proposalID` (`proposalId`),
  KEY `fk_privilege_discussionGroup1_idx` (`discussionGroupId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `proposal`
--

CREATE TABLE IF NOT EXISTS `proposal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` text,
  `status` int(11) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `collaborationUserId` bigint(20) NOT NULL,
  `validFrom` timestamp NULL DEFAULT NULL,
  `validTo` timestamp NULL DEFAULT NULL,
  `statusDate` timestamp NULL DEFAULT NULL,
  `note` text,
  `contentType` varchar(30) DEFAULT NULL,
  `vocabularyId` bigint(20) unsigned DEFAULT NULL,
  `vocabularyIdTwo` bigint(20) unsigned DEFAULT NULL,
  `vocabularyName` varchar(64) DEFAULT NULL,
  `vocabularyNameTwo` varchar(64) DEFAULT NULL,
  `lastChangeDate` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `proposalobject`
--

CREATE TABLE IF NOT EXISTS `proposalobject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `proposalId` bigint(20) NOT NULL,
  `changeType` int(11) DEFAULT NULL,
  `classId` bigint(20) DEFAULT NULL,
  `classId2` bigint(20) DEFAULT NULL,
  `classname` text,
  `name` text,
  PRIMARY KEY (`id`),
  KEY `proposalID` (`proposalId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `proposalstatuschange`
--

CREATE TABLE IF NOT EXISTS `proposalstatuschange` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `proposalStatusFrom` int(11) NOT NULL,
  `proposalStatusTo` int(11) NOT NULL,
  `changeTimestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `collaborationUserId` bigint(20) NOT NULL,
  `proposalId` bigint(20) NOT NULL,
  `reason` text,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`),
  KEY `proposalID` (`proposalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `quote`
--

CREATE TABLE IF NOT EXISTS `quote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `discussionId` bigint(20) DEFAULT NULL,
  `discussionIdQuoted` bigint(20) DEFAULT NULL,
  `text` text,
  PRIMARY KEY (`id`),
  KEY `discussionIDQuoted` (`discussionIdQuoted`),
  KEY `discussionID` (`discussionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `rating`
--

CREATE TABLE IF NOT EXISTS `rating` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `collaborationUserId` bigint(20) NOT NULL,
  `discussionId` bigint(20) DEFAULT NULL,
  `negative` int(11) DEFAULT NULL,
  `positive` int(11) DEFAULT NULL,
  `text` text,
  `value` int(11) DEFAULT NULL,
  `proposalId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`),
  KEY `discussionID` (`discussionId`),
  KEY `FK_rating_Proposal` (`proposalId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `role`
--

CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adminFlag` tinyint(1) DEFAULT '0',
  `name` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Daten für Tabelle `role`
--

INSERT INTO `role` (`id`, `adminFlag`, `name`) VALUES
(1, 0, 'Diskussionsteilnehmer'),
(2, 0, 'Inhaltsverwalter'),
(3, 0, 'Rezensent'),
(4, 1, 'Terminologieadministrator');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `role2action`
--

CREATE TABLE IF NOT EXISTS `role2action` (
  `statusRelId` bigint(20) NOT NULL,
  `roleId` bigint(20) NOT NULL,
  PRIMARY KEY (`statusRelId`,`roleId`),
  KEY `roleID` (`roleId`),
  KEY `statusRelID` (`statusRelId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `role2action`
--

INSERT INTO `role2action` (`statusRelId`, `roleId`) VALUES
(1, 2),
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(6, 2),
(1, 4),
(2, 4),
(3, 4),
(4, 4),
(5, 4),
(6, 4),
(7, 4),
(8, 4),
(9, 4),
(10, 4),
(11, 4),
(12, 4);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `role2collaborationuser`
--

CREATE TABLE IF NOT EXISTS `role2collaborationuser` (
  `collaborationUserId` bigint(20) NOT NULL,
  `roleId` bigint(20) NOT NULL,
  PRIMARY KEY (`roleId`,`collaborationUserId`),
  KEY `collaborationUserId` (`collaborationUserId`),
  KEY `roleId` (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `role2collaborationuser`
--

INSERT INTO `role2collaborationuser` (`collaborationUserId`, `roleId`) VALUES
(11, 4),
(16, 2),
(17, 1),
(18, 1),
(19, 1),
(20, 2),
(21, 1),
(22, 1),
(23, 1),
(24, 2),
(26, 4),
(27, 1),
(28, 1),
(29, 1),
(30, 4);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `role2status`
--

CREATE TABLE IF NOT EXISTS `role2status` (
  `roleId` bigint(20) NOT NULL,
  `statusId` bigint(20) NOT NULL,
  PRIMARY KEY (`roleId`,`statusId`),
  KEY `fk_role_has_status_status1_idx` (`statusId`),
  KEY `fk_role_has_status_role1_idx` (`roleId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `status`
--

CREATE TABLE IF NOT EXISTS `status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isPublic` tinyint(1) DEFAULT '0',
  `isDeleted` tinyint(1) DEFAULT '0',
  `status` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

--
-- Daten für Tabelle `status`
--

INSERT INTO `status` (`id`, `isPublic`, `isDeleted`, `status`) VALUES
(1, 0, 0, 'vorgeschlagen'),
(2, 0, 0, 'in Diskussion'),
(3, 0, 0, 'in Produktion'),
(4, 0, 1, 'abgelehnt'),
(5, 1, 0, 'in Publikation'),
(6, 0, 0, 'gesperrt'),
(7, 0, 0, 'deaktiviert'),
(8, 0, 0, 'eingefroren');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `statusrel`
--

CREATE TABLE IF NOT EXISTS `statusrel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `statusIdFrom` bigint(20) NOT NULL,
  `statusIdTo` bigint(20) NOT NULL,
  `actionId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `actionID` (`actionId`),
  KEY `statusIDFrom` (`statusIdFrom`),
  KEY `statusIDTo` (`statusIdTo`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=14 ;

--
-- Daten für Tabelle `statusrel`
--

INSERT INTO `statusrel` (`id`, `statusIdFrom`, `statusIdTo`, `actionId`) VALUES
(1, 1, 4, 1),
(2, 1, 2, 2),
(3, 1, 3, 3),
(4, 2, 4, 1),
(5, 2, 3, 3),
(6, 3, 4, 1),
(7, 3, 5, 4),
(8, 5, 6, 6),
(9, 5, 8, 7),
(10, 5, 7, 8),
(11, 6, 5, 5),
(12, 8, 7, 8);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `sys_param`
--

CREATE TABLE IF NOT EXISTS `sys_param` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `validity_domain` bigint(20) DEFAULT NULL,
  `object_id` bigint(20) DEFAULT NULL,
  `modify_level` bigint(20) DEFAULT NULL,
  `java_datatype` text,
  `value` text,
  `description` text,
  PRIMARY KEY (`id`),
  KEY `FK200D2C9B8AE2A0D7_idx` (`modify_level`),
  KEY `FK200D2C9B29259B89_idx` (`validity_domain`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Daten für Tabelle `sys_param`
--

INSERT INTO `sys_param` (`id`, `name`, `validity_domain`, `object_id`, `modify_level`, `java_datatype`, `value`, `description`) VALUES
(1, 'mail_sender', 1313, NULL, 1313, 'string', 'noreply.test@gesundheit.gv.at', NULL),
(2, 'mail_host', 1313, NULL, 1313, 'string', 'smtp.gmail.com', NULL),
(4, 'mail_user', 1313, NULL, 1313, 'string', 'ehealthcore', NULL),
(5, 'mail_password', 1313, NULL, 1313, 'string', 'taYAsWa3', NULL),
(6, 'mail_port', 1313, NULL, 1313, 'string', '465', NULL),
(7, 'mail_ssl_enable', 1313, NULL, 1313, 'string', 'true', NULL),
(8, 'mail_name', 1313, NULL, 1313, 'string', 'Kollaborationsumgebung des Terminologieservers', NULL),
(9, 'weblink', 1313, NULL, 1313, 'string', 'http://localhost:8080/TermBrowser', NULL),
(10, 'mail_auth', 1313, NULL, 1313, 'string', 'true', NULL);

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `assigned_term`
--
ALTER TABLE `assigned_term`
  ADD CONSTRAINT `assigned_term_ibfk_1` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`);

--
-- Constraints der Tabelle `class_attribute`
--
ALTER TABLE `class_attribute`
  ADD CONSTRAINT `fk_class_attribute_domain1` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `collaborationuser`
--
ALTER TABLE `collaborationuser`
  ADD CONSTRAINT `fk_collaborationuser_organisation1` FOREIGN KEY (`organisation_ID`) REFERENCES `organisation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `discussion`
--
ALTER TABLE `discussion`
  ADD CONSTRAINT `FK_Discussion_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`),
  ADD CONSTRAINT `FK_Discussion_Proposal` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`),
  ADD CONSTRAINT `FK_Discussion_ProposalObject` FOREIGN KEY (`proposalObjectId`) REFERENCES `proposalobject` (`id`);

--
-- Constraints der Tabelle `discussiongroup2collaborationuser`
--
ALTER TABLE `discussiongroup2collaborationuser`
  ADD CONSTRAINT `fk_discussionGroup_has_collaborationuser_collaborationuser1` FOREIGN KEY (`collaborationuserId`) REFERENCES `collaborationuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_discussionGroup_has_collaborationuser_discussionGroup1` FOREIGN KEY (`discussionGroupId`) REFERENCES `discussiongroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `discussiongroupobject`
--
ALTER TABLE `discussiongroupobject`
  ADD CONSTRAINT `discussiongroupobject_ibfk_1` FOREIGN KEY (`discussionGroup_id`) REFERENCES `discussiongroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `domain`
--
ALTER TABLE `domain`
  ADD CONSTRAINT `fk_domain_domain_value1` FOREIGN KEY (`default_value_id`) REFERENCES `domain_value` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_domain_domain_value2` FOREIGN KEY (`display_order`) REFERENCES `domain_value` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `domain_value`
--
ALTER TABLE `domain_value`
  ADD CONSTRAINT `domain_fk` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `domainvalue2domainvalue`
--
ALTER TABLE `domainvalue2domainvalue`
  ADD CONSTRAINT `fk_domainvalue2domainvalue_domain_value1` FOREIGN KEY (`domain_value_id_parent`) REFERENCES `domain_value` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_domainvalue2domainvalue_domain_value2` FOREIGN KEY (`domain_value_id_child`) REFERENCES `domain_value` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `enquiry`
--
ALTER TABLE `enquiry`
  ADD CONSTRAINT `enquiry_ibfk_1` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `enquiry_ibfk_2` FOREIGN KEY (`extPerson`) REFERENCES `collaborationuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `link`
--
ALTER TABLE `link`
  ADD CONSTRAINT `FK_Link_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`),
  ADD CONSTRAINT `FK_Link_Discussion` FOREIGN KEY (`discussionId`) REFERENCES `discussion` (`id`),
  ADD CONSTRAINT `FK_Link_Proposal` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`);

--
-- Constraints der Tabelle `privilege`
--
ALTER TABLE `privilege`
  ADD CONSTRAINT `FK_Privilege_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`),
  ADD CONSTRAINT `fk_privilege_discussionGroup1` FOREIGN KEY (`discussionGroupId`) REFERENCES `discussiongroup` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FK_Privilege_Proposal` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`);

--
-- Constraints der Tabelle `proposal`
--
ALTER TABLE `proposal`
  ADD CONSTRAINT `FK_Proposal_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`);

--
-- Constraints der Tabelle `proposalobject`
--
ALTER TABLE `proposalobject`
  ADD CONSTRAINT `FK_ProposalObject_Proposal` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`);

--
-- Constraints der Tabelle `proposalstatuschange`
--
ALTER TABLE `proposalstatuschange`
  ADD CONSTRAINT `FK_poposalstatuschange_2` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`),
  ADD CONSTRAINT `FK_PoposalStatusChange_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`);

--
-- Constraints der Tabelle `quote`
--
ALTER TABLE `quote`
  ADD CONSTRAINT `FK_Quote_Discussion` FOREIGN KEY (`discussionId`) REFERENCES `discussion` (`id`),
  ADD CONSTRAINT `FK_Quote_DiscussionQuoted` FOREIGN KEY (`discussionIdQuoted`) REFERENCES `discussion` (`id`);

--
-- Constraints der Tabelle `rating`
--
ALTER TABLE `rating`
  ADD CONSTRAINT `FK_Rating_CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`),
  ADD CONSTRAINT `FK_Rating_Discussion` FOREIGN KEY (`discussionId`) REFERENCES `discussion` (`id`),
  ADD CONSTRAINT `FK_rating_Proposal` FOREIGN KEY (`proposalId`) REFERENCES `proposal` (`id`);

--
-- Constraints der Tabelle `role2action`
--
ALTER TABLE `role2action`
  ADD CONSTRAINT `FK_role2action_2` FOREIGN KEY (`statusRelId`) REFERENCES `statusrel` (`id`),
  ADD CONSTRAINT `FK_Role2Action_Role` FOREIGN KEY (`roleId`) REFERENCES `role` (`id`);

--
-- Constraints der Tabelle `role2collaborationuser`
--
ALTER TABLE `role2collaborationuser`
  ADD CONSTRAINT `CollaborationUser` FOREIGN KEY (`collaborationUserId`) REFERENCES `collaborationuser` (`id`),
  ADD CONSTRAINT `Role` FOREIGN KEY (`roleId`) REFERENCES `role` (`id`);

--
-- Constraints der Tabelle `role2status`
--
ALTER TABLE `role2status`
  ADD CONSTRAINT `fk_role_has_status_role1` FOREIGN KEY (`roleId`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_role_has_status_status1` FOREIGN KEY (`statusId`) REFERENCES `status` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `statusrel`
--
ALTER TABLE `statusrel`
  ADD CONSTRAINT `FK_StatusRel_Action` FOREIGN KEY (`actionId`) REFERENCES `action` (`id`),
  ADD CONSTRAINT `FK_StatusRel_Status` FOREIGN KEY (`statusIdFrom`) REFERENCES `status` (`id`),
  ADD CONSTRAINT `FK_StatusRel_StatusTo` FOREIGN KEY (`statusIdTo`) REFERENCES `status` (`id`);

--
-- Constraints der Tabelle `sys_param`
--
ALTER TABLE `sys_param`
  ADD CONSTRAINT `FK200D2C9B29259B89` FOREIGN KEY (`validity_domain`) REFERENCES `domain_value` (`id`),
  ADD CONSTRAINT `FK200D2C9B8AE2A0D7` FOREIGN KEY (`modify_level`) REFERENCES `domain_value` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
