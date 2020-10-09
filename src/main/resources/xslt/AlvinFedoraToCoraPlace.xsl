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
	<xsl:template match="/">
		<xsl:apply-templates select="place" />
	</xsl:template>
	<xsl:template match="place">
		<authority type="place">
			<xsl:apply-templates select="recordInfo">
				<xsl:with-param name="authorityType" select="'place'"></xsl:with-param>
			</xsl:apply-templates>
			<xsl:apply-templates select="defaultPlaceName" />
			<xsl:apply-templates select="placeNameForms" />
			<xsl:if
				test="string-length(longitude) &gt; 0 or string-length(latitude) &gt; 0">
				<xsl:call-template name="coordinates" />
			</xsl:if>
			<xsl:apply-templates select="country" />
			<xsl:apply-templates select="localIdentifiers" />
		</authority>
	</xsl:template>
	<xsl:template match="recordInfo">
		<xsl:param name="authorityType"></xsl:param>
		<recordInfo>
			<id>
				<xsl:value-of select="../pid"></xsl:value-of>
			</id>
			<type>
				<linkedRecordType>recordType</linkedRecordType>
				<linkedRecordId>
					<xsl:value-of select="$authorityType"></xsl:value-of>
				</linkedRecordId>
			</type>
			<createdBy>
				<linkedRecordType>user</linkedRecordType>
					<!-- Not implemented correctly yet. Currently hardcoded value 12345 -->
					<!-- <xsl:value-of select="created/user/userId"></xsl:value-of> -->
				<linkedRecordId>12345</linkedRecordId>
			</createdBy>
			<tsCreated>
				<xsl:call-template name="formatDateWithTimeZone">
					<xsl:with-param name="dateWithTimezone"
						select="created/date" />
				</xsl:call-template>
			</tsCreated>
			<dataDivider>
				<linkedRecordType>system</linkedRecordType>
				<linkedRecordId>alvin</linkedRecordId>
			</dataDivider>
			<xsl:for-each
				select="created[type = 'CREATED'] | updated/userAction[type = 'UPDATED']">
				<updated>
					<xsl:attribute name="repeatId">
                        <xsl:value-of
						select="position() - 1"></xsl:value-of>
                    </xsl:attribute>
					<updatedBy>
						<linkedRecordType>user</linkedRecordType>
						<linkedRecordId>12345</linkedRecordId>
					</updatedBy>
					<tsUpdated>
						<!-- <xsl:value-of select="date"></xsl:value-of> -->
						<xsl:call-template name="formatDateWithTimeZone">
							<xsl:with-param name="dateWithTimezone"
								select="date" />
						</xsl:call-template>
					</tsUpdated>
				</updated>
			</xsl:for-each>
		</recordInfo>
	</xsl:template>
	<xsl:template match="defaultPlaceName">
		<name type="authorized">
			<namePart type="defaultName">
				<value>
					<xsl:value-of select="name"></xsl:value-of>
				</value>
			</namePart>
		</name>
	</xsl:template>
	<xsl:template match="placeNameForms">
		<xsl:for-each select="entry">
			<name type="alternative">
				<xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
				<xsl:for-each select="placeNameForm">
					<xsl:apply-templates select="language"></xsl:apply-templates>
					<namePart type="defaultName">
						<value>
							<xsl:value-of select="name"></xsl:value-of>
						</value>
					</namePart>
				</xsl:for-each>
			</name>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="coordinates">
		<xsl:if test="latitude and longitude">
			<coordinates>
				<xsl:for-each select="latitude">
					<latitude>
						<xsl:value-of select="."></xsl:value-of>
					</latitude>
				</xsl:for-each>
				<xsl:for-each select="longitude">
					<longitude>
						<xsl:value-of select="."></xsl:value-of>
					</longitude>
				</xsl:for-each>
			</coordinates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="country">
		<xsl:choose>
			<xsl:when test="@class = 'historicCountry'">
				<historicCountry>
					<xsl:value-of select="code"></xsl:value-of>
				</historicCountry>
			</xsl:when>
			<xsl:otherwise>
				<country>
					<xsl:value-of select="alpha2Code"></xsl:value-of>
				</country>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="language">
		<language>
			<xsl:value-of select="alpha3Code"></xsl:value-of>
		</language>
	</xsl:template>
	<xsl:template match="localIdentifiers">
		<xsl:for-each select="localIdentifier/type">
			<identifier>
				<xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
				<identifierType>
					<xsl:value-of select="code"></xsl:value-of>
				</identifierType>
				<identifierValue>
					<xsl:value-of select="../text"></xsl:value-of>
				</identifierValue>
			</identifier>
		</xsl:for-each>
	</xsl:template>
	<!-- DATE TEMPLATE, should be extrated and added as a library -->
	<!-- Expected format for dateWithTimezone : yyyy-mm-dd hh:mm:ss.SSS UTC -->
	<xsl:template name="formatDateWithTimeZone">
		<xsl:param name="dateWithTimezone" />

		<xsl:variable name="date">
			<xsl:value-of select="substring($dateWithTimezone,1,10)" />
		</xsl:variable>

		<xsl:variable name="time">
			<xsl:value-of
				select="substring($dateWithTimezone,12,8)" />
		</xsl:variable>
		<xsl:variable name="milliseconds">
			<xsl:value-of
				select="translate(substring($dateWithTimezone,21,3),'0123456789 UTC', '01234567890000')" />
		</xsl:variable>

		
		<xsl:value-of select="concat($date,'T', $time,'.',$milliseconds,'000Z')" />
<!-- 		<xsl:value-of select="concat($date,' ', $time)" /> -->
	</xsl:template>
</xsl:stylesheet>