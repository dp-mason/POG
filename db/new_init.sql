DROP DATABASE IF EXISTS pogdb;
CREATE DATABASE IF NOT EXISTS pogdb;

DROP TABLE IF EXISTS pogdb.papers CASCADE;
CREATE TABLE `pogdb`.`papers` (
  `paper_id` VARCHAR(13) NOT NULL,
  `title` VARCHAR(256) NULL,
  `year` SMALLINT NULL,
  `doc_url` VARCHAR(105) NULL,
  `source_url` VARCHAR(105) NULL,
  `summary` VARCHAR(350) NULL,
  `cited_by_url` VARCHAR(105) NULL,
  `count` INT NULL DEFAULT 0,
  PRIMARY KEY (`paper_id`))
  ENGINE = InnoDB;

DROP TABLE IF EXISTS pogdb.authors CASCADE;
CREATE TABLE `pogdb`.`authors` (
  `author_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `author_url` VARCHAR(105) NULL,
  `author_fname` VARCHAR(50) NULL,
  `author_lname` VARCHAR(50) NULL,
  PRIMARY KEY (`author_id`))
  ENGINE = InnoDB;

CREATE TABLE `pogdb`.`authored_by` (
  `paper_id` VARCHAR(13) NOT NULL,
  `author_id` INT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`paper_id`, `author_id`),
  INDEX `author_id_idx` (`author_id` ASC) VISIBLE,
  CONSTRAINT `paper_id`
    FOREIGN KEY (`paper_id`)
    REFERENCES `pogdb`.`papers` (`paper_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `author_id`
    FOREIGN KEY (`author_id`)
    REFERENCES `pogdb`.`authors` (`author_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE TABLE `pogdb`.`cited_by` (
  `cited` VARCHAR(13) NOT NULL,
  `citer` VARCHAR(13) NOT NULL,
  PRIMARY KEY (`cited`, `citer`),
  INDEX `citer_idx` (`citer` ASC) VISIBLE,
  CONSTRAINT `cited`
    FOREIGN KEY (`cited`)
    REFERENCES `pogdb`.`papers` (`paper_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `citer`
    FOREIGN KEY (`citer`)
    REFERENCES `pogdb`.`papers` (`paper_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;

CREATE TABLE `pogdb`.`citations` (
  `paperid` VARCHAR(13) NOT NULL,
  `mla` VARCHAR(100) NULL,
  `apa` VARCHAR(100) NULL,
  `chicago` VARCHAR(100) NULL,
  PRIMARY KEY (`paperid`),
  CONSTRAINT `paperid`
    FOREIGN KEY (`paperid`)
    REFERENCES `pogdb`.`papers` (`paper_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;