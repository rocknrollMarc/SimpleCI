CREATE TABLE IF NOT EXISTS `jobs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID of Job',
  `name` text NOT NULL COMMENT 'Job Name',
  `status` int(11) NOT NULL COMMENT 'Status of Job',
  `lastRevision` text NOT NULL COMMENT 'Last Revision for SCM',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Job List' AUTO_INCREMENT=10 ;

CREATE TABLE IF NOT EXISTS `job_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID of this Row',
  `job_id` int(11) NOT NULL COMMENT 'Job ID',
  `status` int(11) NOT NULL COMMENT 'Job Status',
  `log` longtext NOT NULL COMMENT 'Job Log',
  `logged` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp of Job History Entry',
  `number` int(11) NOT NULL COMMENT 'Build Number',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Job History' AUTO_INCREMENT=15 ;