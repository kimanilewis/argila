-- MySQL dump 10.13  Distrib 5.5.50, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: argila
-- ------------------------------------------------------
-- Server version	5.5.50-0ubuntu0.14.04.1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `coreRequests`
--

DROP TABLE IF EXISTS `coreRequests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coreRequests` (
  `coreRequestID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `accountNumber` varchar(45) NOT NULL,
  `accountBalance` double(15,2) NOT NULL DEFAULT '0.00',
  `MSISDN` bigint(20) DEFAULT NULL,
  `dateCreated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(1) NOT NULL DEFAULT '0',
  `originIP` int(11) DEFAULT NULL,
  PRIMARY KEY (`coreRequestID`),
  KEY `accountNumber` (`accountNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coreRequests`
--

LOCK TABLES `coreRequests` WRITE;
/*!40000 ALTER TABLE `coreRequests` DISABLE KEYS */;
/*!40000 ALTER TABLE `coreRequests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customerProfileAccounts`
--

DROP TABLE IF EXISTS `customerProfileAccounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customerProfileAccounts` (
  `customerProfileAccountID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `customerProfileID` int(11) NOT NULL,
  `availableTime` smallint(6) NOT NULL DEFAULT '0',
  `startTime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `expiryTime` datetime DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '2',
  `dateModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dateCreated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`customerProfileAccountID`),
  KEY `customerProfileID` (`customerProfileID`),
  CONSTRAINT `fk_customerProfileID` FOREIGN KEY (`customerProfileID`) REFERENCES `customerProfiles` (`customerProfileID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customerProfileAccounts`
--

LOCK TABLES `customerProfileAccounts` WRITE;
/*!40000 ALTER TABLE `customerProfileAccounts` DISABLE KEYS */;
INSERT INTO `customerProfileAccounts` VALUES (1,1,1000,'2016-08-05 20:47:19','2016-08-05 22:02:19',2,1,'2016-07-18 15:59:59','2016-07-18 18:59:59'),(2,2,30,'2016-07-18 19:00:06','2016-07-18 19:00:06',2,1,'2016-07-18 16:00:06','2016-07-18 19:00:06'),(3,3,97,'2016-08-06 20:44:18','2016-08-06 20:44:22',0,2,'2016-08-05 18:16:33','2016-08-05 21:16:33');
/*!40000 ALTER TABLE `customerProfileAccounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customerProfiles`
--

DROP TABLE IF EXISTS `customerProfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customerProfiles` (
  `customerProfileID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `accountNumber` varchar(45) NOT NULL,
  `MSISDN` bigint(20) NOT NULL,
  `amount` double(15,2) NOT NULL DEFAULT '0.00',
  `statusCode` smallint(1) NOT NULL DEFAULT '0',
  `statusDescription` text,
  `active` tinyint(1) NOT NULL DEFAULT '2',
  `dateCreated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customerProfileID`),
  UNIQUE KEY `accountNumber` (`accountNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customerProfiles`
--

LOCK TABLES `customerProfiles` WRITE;
/*!40000 ALTER TABLE `customerProfiles` DISABLE KEYS */;
INSERT INTO `customerProfiles` VALUES (1,'212135K',254718668088,1000.00,2,'null',2,'2016-07-18 18:56:07','2016-07-18 15:56:07'),(2,'212145L',254711123456,4000.00,0,'null',2,'2016-07-18 18:56:34','2016-07-18 15:56:34'),(3,'D55A1653',254718668308,100.00,2,NULL,2,'2016-08-05 21:14:22','2016-08-05 18:14:22');
/*!40000 ALTER TABLE `customerProfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessionData`
--

DROP TABLE IF EXISTS `sessionData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessionData` (
  `sessionDataID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `customerProfileAccountID` int(11) unsigned NOT NULL,
  `timeSpent` text,
  `amountSpent` double(15,2) NOT NULL DEFAULT '0.00',
  `syncStatus` tinyint(1) NOT NULL DEFAULT '0',
  `amountBalance` double(15,2) NOT NULL DEFAULT '0.00',
  `dateCreated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sessionDataID`),
  KEY `customerProfileAccountID` (`customerProfileAccountID`),
  CONSTRAINT `fk_customerProfileAccountID` FOREIGN KEY (`customerProfileAccountID`) REFERENCES `customerProfileAccounts` (`customerProfileAccountID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessionData`
--

LOCK TABLES `sessionData` WRITE;
/*!40000 ALTER TABLE `sessionData` DISABLE KEYS */;
INSERT INTO `sessionData` VALUES (1,3,'60',60.00,2,40.00,'2016-08-06 12:44:24','2016-08-06 09:44:24'),(2,1,'75',75.00,2,925.00,'2016-08-06 17:05:07','2016-08-06 14:05:07');
/*!40000 ALTER TABLE `sessionData` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `statusCodes`
--

DROP TABLE IF EXISTS `statusCodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `statusCodes` (
  `statusID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `statusCodeID` int(11) NOT NULL,
  `statusName` varchar(45) DEFAULT NULL,
  `statusDescription` varchar(150) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `dateCreated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`statusID`),
  KEY `statusCodeID` (`statusCodeID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `statusCodes`
--

LOCK TABLES `statusCodes` WRITE;
/*!40000 ALTER TABLE `statusCodes` DISABLE KEYS */;
INSERT INTO `statusCodes` VALUES (1,0,'unprocessed','new record waiting processing',1,'2016-07-12 00:00:00','2016-07-12 07:34:12'),(2,1,'Processing','record picked for processing',1,'2016-07-12 00:00:00','2016-07-12 07:34:12'),(3,2,'processed','Record has been processed successfully',1,'2016-07-12 00:00:00','2016-07-12 08:27:27'),(4,3,'Processing finished','Processing finished',1,'2016-07-12 00:00:00','2016-07-12 08:27:32');
/*!40000 ALTER TABLE `statusCodes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-08-06 20:49:42
