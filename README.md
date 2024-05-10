# SJavaClass

# Changes:
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