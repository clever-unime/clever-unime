-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generato il: Feb 03, 2014 alle 17:32
-- Versione del server: 5.5.34
-- Versione PHP: 5.3.10-1ubuntu3.9

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `sensordb`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `misura_anagrafica`
--

CREATE TABLE IF NOT EXISTS `misura_anagrafica` (
  `idmisura_anagrafica` int(11) NOT NULL AUTO_INCREMENT,
  `id_uom_tipomisura` varchar(45) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `mobile` tinyint(4) DEFAULT NULL,
  `frequenza` float DEFAULT NULL,
  `frequenza_uom` varchar(10) DEFAULT NULL,
  `tipo_misura` varchar(30) DEFAULT NULL,
  `tipo_misura_uom` varchar(10) DEFAULT NULL,
  `sensore_anagrafica_idsensore_anagrafica` int(11) NOT NULL,
  PRIMARY KEY (`idmisura_anagrafica`),
  KEY `fk_idmisura_anagrafica_sensore_anagrafica2_idx` (`sensore_anagrafica_idsensore_anagrafica`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=24 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `osservazioni`
--

CREATE TABLE IF NOT EXISTS `osservazioni` (
  `idosservazioni` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp_osservazione` varchar(45) DEFAULT NULL,
  `valore_osservato` varchar(45) DEFAULT NULL,
  `misura_anagrafica_idmisura_anagrafica` int(11) NOT NULL,
  `sensore_anagrafica_idsensore_anagrafica` int(11) NOT NULL,
  PRIMARY KEY (`idosservazioni`,`misura_anagrafica_idmisura_anagrafica`,`sensore_anagrafica_idsensore_anagrafica`),
  KEY `fk_osservazioni_misura_anagrafica1_idx` (`misura_anagrafica_idmisura_anagrafica`),
  KEY `fk_osservazioni_sensore_anagrafica1_idx` (`sensore_anagrafica_idsensore_anagrafica`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3047 ;

-- --------------------------------------------------------

--
-- Struttura della tabella `sensore_anagrafica`
--

CREATE TABLE IF NOT EXISTS `sensore_anagrafica` (
  `idsensore_anagrafica` int(11) NOT NULL AUTO_INCREMENT,
  `latitudine` double NOT NULL,
  `longitudine` double NOT NULL,
  `altitudine` float NOT NULL,
  `latitudine_uom` varchar(15) NOT NULL,
  `longitudine_uom` varchar(15) NOT NULL,
  `altitudine_uom` varchar(15) NOT NULL,
  `descrizione` varchar(45) DEFAULT NULL,
  `id_tipo` varchar(45) NOT NULL,
  `costruttore` varchar(30) NOT NULL,
  `modello` varchar(25) DEFAULT NULL,
  `intervallo_misura` int(11) NOT NULL,
  `intervallo_misura_uom` varchar(7) NOT NULL,
  `packet` varchar(300) NOT NULL,
  `tipo_frame` char(1) NOT NULL,
  `tipo_nodo` char(1) NOT NULL,
  `id_device` tinyint(4) DEFAULT NULL,
  `versione_firmware` varchar(45) DEFAULT NULL,
  `operatorArea` varchar(70) DEFAULT NULL,
  `active` char(1) NOT NULL DEFAULT 's',
  `mobile` char(1) NOT NULL,
  PRIMARY KEY (`idsensore_anagrafica`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=24 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
