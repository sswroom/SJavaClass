# SJavaClass

# Changes:
- Added SystemInfoUtil.getComputerName
- Fix MemorySessionManager error

0.5.20241121
- Added StringUtil.utf32CharCnt
- Added StringUtil.appendUTF32Char
- Added StringUtil.indexOfUTF8
- Added UTF32Reader
- Added HTTPProxyTCPClient
- Added JasperUtil.styledCJKBString
- Change TCPClient values to protected
- Added HTTPMyClient
- Enhance HTTPClient
- Added SocketUtil.getIPv4Name(int)
- Added SocketUtil.getAddr
- Added SSLEngine.clientSetCertASN1
- Enhance TCPClient to support timeout
- Added TCPClient.isSSL, getRemoteCerts, replaceSocket
- Added TCPClientFactory
- Added ByteTool.intOr
- Added FileUtil.parseCmdLine
- Added FileUtil.getArgs
- Added MyStream
- Added Path.getProcessFileName
- Added Path.appendPath
- UTF8Reader support IOStream
- Added RequestMethod.HTTP_HEAD, HTTP_OPTIONS, HTTP_TRACE
- Added SSLEngine.clientConnect
- Change IAMSmartAPI to use TCPClientFactory

0.5.20241111
- Fix MQTTClient event handling

0.5.20241109
- Added LogTool.logStackTrace
- Added TaskMQTTManager.setLog
- Remove some warnings

0.5.20241107
- Enhance Image parsing
- Added DateTimeUtil.toString(LocalDate, String)
- Added TimedTask.close
- Fix BinaryParser.nextStr problem
- Fix BinaryParser.nextBool problem
- Enhance DateTimeUtil.newInstance exception message

0.5.20241015
- Added HTTPServerUtil.getForwardedAddr
- Remove default password

0.5.20241010
- Change Bcrypt to Nonnull with throwing IllegalArgumentException
- Change CertUtil.loadPrivateKey to allow null password
- Change BinaryBuilder.appendIPAddr to allow null
- Added DataTools.createStringMapOrNull
- Change JasperFontCalculator.calcTextFieldBySize to Nonnull

0.4.20241009
- Added BinaryBuilder
- Added BinaryParser
- Added StringUtil.writeChar
- Added DateTimeUtil.getTZQhr, fromTZQhr
- Added DateTimeUtil.newInstant
- Added DateTimeUtil.newZonedDateTime(Instant, byte tzQhr)
- Added DateTimeUtil.getTotalDays, newLocalDate
- Added DateTimeUtil.isYearLeap
- Added DateTimeUtil.date2TotalDays, totalDays2DateValue
- Added DateTimeUtil.toDate
- Added CRC32R.calcDirect
- Added HTTPServerUtil.isForwardedSSL
- MemoryWebSessionManager support forward ssl detection

0.4.20241003
- Added Nonnull/Nullable
- Added ArcGISRESTAPI.getUserToken

0.4.20240923
- Update dependency version

0.4.20240917
- Fixed GeometryUtil.toVector2D Polygon problem
- Added DateTimeUtil.equals
- Added DBUtil.dbVec
- Enhancing SQLReader.getVector
- Added WKBReader.java
- Added WKBWriter.java

0.4.20240913
- Fixed DateTimeUtil.toString timezone problem
- Fixed DateTimeUtil.toString(Date) problem
- Fixed MyX509Key fix cipher creation
- Fixed Radix64Enc.encodeBin
- Fixed IAMSmartAPI.initHTTPClient
- Fixed JasyptEncryptor.decrypt/encrypt padding problem
- Fixed IAMSmart.parseAddress
- Fixed StringUtil.utf16CharCnt
- Added DateTimeUtil.clearDayOfMonth
- Added DateTimeUtil.addMonth
- Added DateTimeUtil.isSameHour
- Added DateTimeUtil.isSameDay(Timestamp, Timestamp)
- DataTimeUtil.isSameDay allow null input
- Added StringUtil.fixedSplit
- Added EmailMessage.addAttachment
- Added StreamDataInputStream
- Fixed ImageParser
- Fixed EXIFData.parseIFD
- Added SocketFactory.setProxy, getProxy
- HTTPOSClient support proxy
- Added ArcGISRESTAPI.setSocketFactory, getClientToken, queryGroups, getGroupUserList

0.4.20240816
- Added BrowserInfo
- Enhance OSInfo
- Added MemoryWebSessionManager
- Remove Encryption encParam
- Change Encryption to abstract class
- Added AES256GCM
- Added Bcrypt.genHash
- Added CipherPadding
- Change Hash to abstract class
- MyX509File.isPublicKeyInfo change to public
- Added MyX509Key.createCipher
- Added MyX509Key.decrypt
- Added MyX509PrivKey.getKeyType
- Added MyX509PrivKey.createKey
- Added MyX509PrivKey.getKeyId
- Added MyX509PubKey.getKeyId
- Added CharUtil.isAlphaNumeric, isDigit, isAlphabet, isUpperCase, isLowerCase
- Added JSONBuilder.objectAddArrayStr, objectAddNFloat64, objectAddNInt32, objectAddBool, objectAddChar, objectAddTSStr, objectAddDateStr, objectAddNull, objectAddGeometry
- Added StringUtil.utf16CharCnt
- Added StringUtil.isUInteger
- Added StringUtil.isHKID
- Added StringUtil.orEmpty
- Added IAMSmartAPI
- Added DateTimeUtil.toString(Timestamp/ZonedDateTime/Date), toYMD
- Added Coord2DDbl.getLat, getLon
- Added HTTPServerUtil
- Added StatusCode
- Split CoordinateSystemManager to ArcGISPRJParser
- Added CoordinateSystemManager.createWGS84Csys
- Added MD5
- Added StaticImage.getExif
- Added JasyptEncrypter.encryptAsB64
- Move X509Parser to parser package
- Added Vector3 get functions
- Added Vector3.toCoord2D, getXY, clone
- FileStream fixed some bugs
- Added OSMCacheHandler
- CoordinateSystem change to use Vector3/Coord2DDbl
- Added CoordinateSystemManager.srCreateCSysOrDef
- EarthEllipsoid change to use Vector3/Coord2DDbl
- ImageList extends ParsedObject
- ImageUtil load with filename
- Added HTTPClient.getContentType
- Added IAMSmartClient
- Added FullParserList
- Added ImageParser
- Rename HTTPMyClient to HTTPOSClient
- Rename XmlUtil.toHTMLText to toHTMLBodyText
- Fixed FileStream.getLength exception
- Added UTF8Reader.isLineBreak
- Fully implement HTTPClient.createClient and createConnect
- HTTPOSClient support userAgent
- EmailMessage change to abstract class
- Added EmailMessage.setSentDate, setMessageId, generateMessageID
- Added EmailTemplate2.java

0.4.20240805
- Migrate from javax.* to jakarta.*

0.3.20240711
- Support null attachment contentId

0.3.20240621
- Fixed compile error

0.3.20240620
- JSONBuilder.java Support more data types
- ArcGISRESTAPI.java fixed non-public data fields

0.3.20240531
- ArcGISTools.java Support non-default schema name

0.3.20240510
- TaskMQTTManager.java Support multimachine task management

0.3.20240509
- FormEncoding.java fix wrong function name to formDecode()
- ArcGISRESTAPI.java added initial version
- HTTPMyClient.java change setDoOutput to method supporting upload only

0.3.20240419
- DBUtil.java getLastIdentity32() support PostgreSQL

0.3.20240418
- ArcGISTools.java support PostgreSQLESRI Type
- DataTools.java toObjectString() enhance Class type display
- DateTimeUtil.java fixed missing nanosec digits on newZonedDateTime()
- DateTimeUtil.java toString() support Date and Time
- DBControl.java fixed wrong string length for non MSSQL type
- DBUtil.java added PostgreSQLESRI Type
- DBUtil.java remove arcGISSDE parameter in dbGeometry(), dbVal()
- DBUtil.java fixed wrong nanosecs values
- JSONMapper.java remove using SimpleDateFormat
- SQLConnection.java support PostgreSQL paging
- SQLReader.java remove arcGIS parameter
- TimestampValidator.java remove using SimpleDateFormat

0.3.20240408
- ArcGISTools.java support Entity without catalog
- DBUtil.java support PostgreSQL style name quotes

0.3.20240405
- Vector3.java added value constructors
- ByteTool.java added listAddArray
- GeometryUtil.java added arcToLine
- ArcGISTools.java support PostgreSQL
- DBUtil.java support geometry values of arcGIS SDE on PostgreSQL
- Coord2DDbl.java added set(Coord2DDbl), add(Coord2DDbl), multiply(Coord2DDbl)
- CoordinateSystem.java change to calSurfaceDistance() and calLineStringDistance()
- EarthEllipsoid.java added calLineStringDistance()
- RectAreaDbl.java change variable names to min/max
- RectAreaDbl.java added all functions from C++ version
- Added CircularString.java
- Added GeometryCollection.java
- Added LinearRing.java
- Added MultiPoint.java
- SQLReader.java support reading ArcGIS SDE geometry on PostgreSQL
- Vector2D.java update structures as C++ version
- WKTWriter.java support reverseAxis

0.2.20240202
- First Release