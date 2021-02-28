CREATE DATABASE IF NOT EXISTS pog;

DROP TABLE IF EXISTS `pog`.`papers` CASCADE;
CREATE TABLE `pog`.`papers` (
  `paper_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `query_url` VARCHAR(100) NULL,
  `title` VARCHAR(50) NULL,
  `year` SMALLINT NULL,
  `doc_url` VARCHAR(75) NULL,
  `source_url` VARCHAR(75) NULL,
  `summary` VARCHAR(128) NULL,
  `cited_by_url` VARCHAR(100) NULL,
  PRIMARY KEY (`paper_id`))
ENGINE = InnoDB;


DROP TABLE IF EXISTS `pog`.`authors`  CASCADE;
CREATE TABLE `pog`.`authors` (
  `author_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `author_url` VARCHAR(75) NULL,
  `author_fname` VARCHAR(45) NULL,
  `author_lname` VARCHAR(45) NULL,
  PRIMARY KEY (`author_id`))
ENGINE = InnoDB;

DROP TABLE IF EXISTS `pog`.`authored_by`;
CREATE TABLE `pog`.`authored_by` (
  `paper_id` INT(10) UNSIGNED NOT NULL,
  `author_id` INT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`paper_id`, `author_id`),
  INDEX `author_id_idx` (`author_id` ASC),
  CONSTRAINT `fk_paper_authored`
    FOREIGN KEY (`paper_id`)
    REFERENCES `pog`.`papers` (`paper_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_author_id`
    FOREIGN KEY (`author_id`)
    REFERENCES `pog`.`authors` (`author_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

DROP TABLE IF EXISTS `pog`.`cited_by` ;
CREATE TABLE `pog`.`cited_by` (
  `cited` INT(10) UNSIGNED NOT NULL,
  `citer` INT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`cited`, `citer`),
  INDEX `citer_idx` (`citer` ASC),
  CONSTRAINT `fk_cited`
    FOREIGN KEY (`cited`)
    REFERENCES `pog`.`papers` (`paper_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_citer`
    FOREIGN KEY (`citer`)
    REFERENCES `pog`.`papers` (`paper_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

DROP TABLE IF EXISTS `pog`.`citations` ;
CREATE TABLE `pog`.`citations` (
  `paper_id` INT(10) UNSIGNED NOT NULL,
  `mla` VARCHAR(100) NULL,
  `apa` VARCHAR(100) NULL,
  `chicago` VARCHAR(100) NULL,
  PRIMARY KEY (`paper_id`),
  CONSTRAINT `fk_paper_id`
    FOREIGN KEY (`paper_id`)
    REFERENCES `pog`.`papers` (`paper_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;