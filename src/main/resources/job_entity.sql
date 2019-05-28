/*
Navicat MySQL Data Transfer

Source Server         : test
Source Server Version : 50719
Source Host           : 127.0.0.1:3306
Source Database       : any

Target Server Type    : MYSQL
Target Server Version : 50719
File Encoding         : 65001

Date: 2019-05-28 10:25:45
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for job_entity
-- ----------------------------
DROP TABLE IF EXISTS `job_entity`;
CREATE TABLE `job_entity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `job_group` varchar(255) NOT NULL,
  `cron` varchar(255) NOT NULL,
  `parameter` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `jar_path` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of job_entity
-- ----------------------------
INSERT INTO `job_entity` VALUES ('1', 'first', 'helloworld', '0 0/1 * * * ? ', '1', '第一个', 'com.m.job.DynamicJob', 'OPEN');
INSERT INTO `job_entity` VALUES ('2', 'second', 'helloworld', '0 0/2 * * * ? ', '2', '第二个', 'com.m.job.DynamicJob2', 'OPEN');
INSERT INTO `job_entity` VALUES ('3', 'third', 'helloworld', '0 0/3 * * * * ? ', '3', '第三个', '', 'CLOSE');
INSERT INTO `job_entity` VALUES ('4', 'four', 'helloworld', '0 0/4 * * * ? *', '4', '第四个', '', 'CLOSE');
