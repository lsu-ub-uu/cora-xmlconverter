<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Copyright 2018 Uppsala University Library
 
  This file is part of Cora.
 
      Cora is free software: you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation, either version 3 of the License, or
      (at your option) any later version.
 
      Cora is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
 
      You should have received a copy of the GNU General Public License
      along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />

	<!-- DATE TEMPLATE, should be extrated and added as a library -->
	<!-- Expected format for dateWithTimezone : yyyy-mm-dd hh:mm:ss.SSS UTC -->
	<xsl:template name="formatDateWithTimeZone">
		<xsl:param name="dateWithTimezone" />

		<xsl:variable name="date">
			<xsl:value-of select="substring($dateWithTimezone,1,10)" />
		</xsl:variable>

		<xsl:variable name="time">
			<xsl:value-of
				select="substring($dateWithTimezone,12,12)" />
		</xsl:variable>

		<xsl:value-of select="concat($date,'T', $time,'000Z')" />
	</xsl:template>

</xsl:stylesheet>