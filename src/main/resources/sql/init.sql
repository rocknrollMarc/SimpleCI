CREATE TABLE IF NOT EXISTS `jobs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID of Job',
  `name` text NOT NULL COMMENT 'Job Name',
  `status` int(11) NOT NULL COMMENT 'Status of Job',
  `lastRevision` text NOT NULL COMMENT 'Last Revision for SCM',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Job List' AUTO_INCREMENT=10 ;