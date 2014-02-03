-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generato il: Feb 03, 2014 alle 17:31
-- Versione del server: 5.5.34
-- Versione PHP: 5.3.10-1ubuntu3.9

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `sensorml`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `classifier`
--

CREATE TABLE IF NOT EXISTS `classifier` (
  `classifier_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `classifier_value` varchar(100) NOT NULL,
  `classifier_description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`classifier_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=56 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `component`
--

CREATE TABLE IF NOT EXISTS `component` (
  `component_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `description` varchar(200) NOT NULL,
  `status` tinyint(1) NOT NULL,
  `mobile` tinyint(1) NOT NULL,
  `crs` varchar(100) NOT NULL,
  `longitude` float NOT NULL,
  `long_uom` varchar(100) NOT NULL,
  `latitude` float NOT NULL,
  `lat_uom` varchar(100) NOT NULL,
  `altitude` float NOT NULL,
  `alt_uom` varchar(100) NOT NULL,
  PRIMARY KEY (`component_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `composite_phenomenon`
--

CREATE TABLE IF NOT EXISTS `composite_phenomenon` (
  `composite_phenomenon_id` int(11) NOT NULL AUTO_INCREMENT,
  `composite_unique_id` varchar(100) NOT NULL,
  `composite_phenomenon_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`composite_phenomenon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `comp_class`
--

CREATE TABLE IF NOT EXISTS `comp_class` (
  `component_id` int(11) NOT NULL,
  `classifier_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`,`classifier_id`),
  KEY `classifier_id` (`classifier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `comp_ident`
--

CREATE TABLE IF NOT EXISTS `comp_ident` (
  `component_id` int(11) NOT NULL,
  `identifier_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`,`identifier_id`),
  KEY `identifier_id` (`identifier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `comp_off`
--

CREATE TABLE IF NOT EXISTS `comp_off` (
  `component_id` int(11) NOT NULL,
  `offering_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`,`offering_id`),
  KEY `offering_id` (`offering_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `comp_phen`
--

CREATE TABLE IF NOT EXISTS `comp_phen` (
  `component_id` int(11) NOT NULL,
  `phenomenon_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`,`phenomenon_id`),
  KEY `phenomenon_id` (`phenomenon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `comp_phen_rel`
--

CREATE TABLE IF NOT EXISTS `comp_phen_rel` (
  `composite_phenomenon_id` int(11) NOT NULL,
  `phenomenon_id` int(11) NOT NULL,
  PRIMARY KEY (`composite_phenomenon_id`,`phenomenon_id`),
  KEY `phenomenon_id` (`phenomenon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `history`
--

CREATE TABLE IF NOT EXISTS `history` (
  `sensor_id` int(11) NOT NULL,
  `time_stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active` tinyint(1) NOT NULL,
  `mobile` tinyint(1) NOT NULL,
  `crs` varchar(100) NOT NULL,
  `longitude` float NOT NULL,
  `latitude` float NOT NULL,
  `altitude` float NOT NULL,
  PRIMARY KEY (`sensor_id`,`time_stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `identifier`
--

CREATE TABLE IF NOT EXISTS `identifier` (
  `identifier_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `identifier_value` varchar(100) NOT NULL,
  `identifier_description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`identifier_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=31 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `observation`
--

CREATE TABLE IF NOT EXISTS `observation` (
  `observation_id` int(11) NOT NULL AUTO_INCREMENT,
  `sensor_id` int(11) NOT NULL,
  `phenomenon_id` int(11) NOT NULL,
  `time_stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `coordinate` point NOT NULL,
  `uom_code` varchar(100) NOT NULL,
  `value` varchar(100) NOT NULL,
  `time_definition` varchar(100) NOT NULL,
  `lat_definition` varchar(100) NOT NULL,
  `long_definition` varchar(100) NOT NULL,
  `long_def_uom` varchar(100) NOT NULL,
  `lat_def_uom` varchar(100) NOT NULL,
  PRIMARY KEY (`observation_id`),
  UNIQUE KEY `UNIQUE` (`sensor_id`,`phenomenon_id`,`time_stamp`),
  KEY `phenomenon_id` (`phenomenon_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1906 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `offering`
--

CREATE TABLE IF NOT EXISTS `offering` (
  `offering_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `offering_name` varchar(200) NOT NULL,
  PRIMARY KEY (`offering_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `phenomenon`
--

CREATE TABLE IF NOT EXISTS `phenomenon` (
  `phenomenon_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `phenomenon_description` varchar(200) NOT NULL,
  `unit` varchar(100) NOT NULL,
  `valuetype` varchar(100) NOT NULL,
  PRIMARY KEY (`phenomenon_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `phen_off`
--

CREATE TABLE IF NOT EXISTS `phen_off` (
  `offering_id` int(11) NOT NULL,
  `phenomenon_id` int(11) NOT NULL,
  PRIMARY KEY (`offering_id`,`phenomenon_id`),
  KEY `phenomenon_id` (`phenomenon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sensor`
--

CREATE TABLE IF NOT EXISTS `sensor` (
  `sensor_id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(100) NOT NULL,
  `description_type` varchar(100) NOT NULL,
  `status` tinyint(1) NOT NULL,
  `mobile` tinyint(1) NOT NULL,
  `srs` varchar(100) NOT NULL,
  `fixed` int(2) NOT NULL,
  `longitude` float NOT NULL,
  `long_uom` varchar(100) NOT NULL,
  `latitude` float NOT NULL,
  `lat_uom` varchar(100) NOT NULL,
  `altitude` float NOT NULL,
  `alt_uom` varchar(100) NOT NULL,
  `coordinate` point NOT NULL,
  `frequency` varchar(100) NOT NULL,
  `frequency_uom` varchar(100) NOT NULL,
  PRIMARY KEY (`sensor_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=49 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `sens_class`
--

CREATE TABLE IF NOT EXISTS `sens_class` (
  `sensor_id` int(11) NOT NULL,
  `classifier_id` int(11) NOT NULL,
  PRIMARY KEY (`classifier_id`,`sensor_id`),
  KEY `sensor_id` (`sensor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sens_comp`
--

CREATE TABLE IF NOT EXISTS `sens_comp` (
  `sensor_id` int(11) NOT NULL,
  `component_id` int(11) NOT NULL,
  PRIMARY KEY (`sensor_id`,`component_id`),
  KEY `component_id` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sens_ident`
--

CREATE TABLE IF NOT EXISTS `sens_ident` (
  `sensor_id` int(11) NOT NULL,
  `identifier_id` int(11) NOT NULL,
  PRIMARY KEY (`identifier_id`,`sensor_id`),
  KEY `sensor_id` (`sensor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sens_off`
--

CREATE TABLE IF NOT EXISTS `sens_off` (
  `sensor_id` int(11) NOT NULL,
  `offering_id` int(11) NOT NULL,
  PRIMARY KEY (`sensor_id`,`offering_id`),
  KEY `offering_id` (`offering_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `sens_phen`
--

CREATE TABLE IF NOT EXISTS `sens_phen` (
  `sensor_id` int(11) NOT NULL,
  `phenomenon_id` int(11) NOT NULL,
  PRIMARY KEY (`sensor_id`,`phenomenon_id`),
  KEY `phenomenon_id` (`phenomenon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `comp_class`
--
ALTER TABLE `comp_class`
  ADD CONSTRAINT `comp_class_ibfk_1` FOREIGN KEY (`component_id`) REFERENCES `component` (`component_id`),
  ADD CONSTRAINT `comp_class_ibfk_2` FOREIGN KEY (`classifier_id`) REFERENCES `classifier` (`classifier_id`);

--
-- Limiti per la tabella `comp_ident`
--
ALTER TABLE `comp_ident`
  ADD CONSTRAINT `comp_ident_ibfk_1` FOREIGN KEY (`component_id`) REFERENCES `component` (`component_id`),
  ADD CONSTRAINT `comp_ident_ibfk_2` FOREIGN KEY (`identifier_id`) REFERENCES `identifier` (`identifier_id`);

--
-- Limiti per la tabella `comp_off`
--
ALTER TABLE `comp_off`
  ADD CONSTRAINT `comp_off_ibfk_1` FOREIGN KEY (`component_id`) REFERENCES `component` (`component_id`),
  ADD CONSTRAINT `comp_off_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`);

--
-- Limiti per la tabella `comp_phen`
--
ALTER TABLE `comp_phen`
  ADD CONSTRAINT `comp_phen_ibfk_1` FOREIGN KEY (`component_id`) REFERENCES `component` (`component_id`),
  ADD CONSTRAINT `comp_phen_ibfk_2` FOREIGN KEY (`phenomenon_id`) REFERENCES `phenomenon` (`phenomenon_id`);

--
-- Limiti per la tabella `comp_phen_rel`
--
ALTER TABLE `comp_phen_rel`
  ADD CONSTRAINT `comp_phen_rel_ibfk_1` FOREIGN KEY (`composite_phenomenon_id`) REFERENCES `composite_phenomenon` (`composite_phenomenon_id`),
  ADD CONSTRAINT `comp_phen_rel_ibfk_2` FOREIGN KEY (`phenomenon_id`) REFERENCES `phenomenon` (`phenomenon_id`);

--
-- Limiti per la tabella `history`
--
ALTER TABLE `history`
  ADD CONSTRAINT `history_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`);

--
-- Limiti per la tabella `observation`
--
ALTER TABLE `observation`
  ADD CONSTRAINT `observation_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`),
  ADD CONSTRAINT `observation_ibfk_2` FOREIGN KEY (`phenomenon_id`) REFERENCES `phenomenon` (`phenomenon_id`);

--
-- Limiti per la tabella `phen_off`
--
ALTER TABLE `phen_off`
  ADD CONSTRAINT `phen_off_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`),
  ADD CONSTRAINT `phen_off_ibfk_2` FOREIGN KEY (`phenomenon_id`) REFERENCES `phenomenon` (`phenomenon_id`);

--
-- Limiti per la tabella `sens_class`
--
ALTER TABLE `sens_class`
  ADD CONSTRAINT `sens_class_ibfk_2` FOREIGN KEY (`classifier_id`) REFERENCES `classifier` (`classifier_id`),
  ADD CONSTRAINT `sens_class_ibfk_3` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`);

--
-- Limiti per la tabella `sens_comp`
--
ALTER TABLE `sens_comp`
  ADD CONSTRAINT `sens_comp_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`),
  ADD CONSTRAINT `sens_comp_ibfk_2` FOREIGN KEY (`component_id`) REFERENCES `component` (`component_id`);

--
-- Limiti per la tabella `sens_ident`
--
ALTER TABLE `sens_ident`
  ADD CONSTRAINT `sens_ident_ibfk_2` FOREIGN KEY (`identifier_id`) REFERENCES `identifier` (`identifier_id`),
  ADD CONSTRAINT `sens_ident_ibfk_3` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`);

--
-- Limiti per la tabella `sens_off`
--
ALTER TABLE `sens_off`
  ADD CONSTRAINT `sens_off_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`),
  ADD CONSTRAINT `sens_off_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`);

--
-- Limiti per la tabella `sens_phen`
--
ALTER TABLE `sens_phen`
  ADD CONSTRAINT `sens_phen_ibfk_1` FOREIGN KEY (`sensor_id`) REFERENCES `sensor` (`sensor_id`),
  ADD CONSTRAINT `sens_phen_ibfk_2` FOREIGN KEY (`phenomenon_id`) REFERENCES `phenomenon` (`phenomenon_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
