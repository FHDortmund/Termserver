-- phpMyAdmin SQL Dump
-- version 3.5.2.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Erstellungszeit: 06. Sep 2013 um 10:00
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
  `sendMail` tinyint(1) DEFAULT NULL,
  `organisation_ID` bigint(20) DEFAULT NULL,
  `activated` tinyint(1) DEFAULT NULL,
  `activation_md5` varchar(80) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  `hidden` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_collaborationuser_organisation1_idx` (`organisation_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=25 ;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
  PRIMARY KEY (`id`),
  KEY `collaborationUserID` (`collaborationUserId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
