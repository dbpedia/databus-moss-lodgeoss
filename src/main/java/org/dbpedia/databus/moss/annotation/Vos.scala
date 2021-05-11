//package org.dbpedia.databus.moss.annotation
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Service
//
//@Service
//class Vos(
//           @Value("${tmp.url}") dbUrl : String,
//           @Value("${tmp.usr}") dbUsr : String,
//           @Value("${tmp.psw}") dbPsw : String) {
//
//
//  def loadModel(graphName: String, model:Model, delete: Boolean = false): Unit = {
//    val db = new VirtDataset(dbUrl,dbUsr,dbPsw)
//    if(delete && db.containsNamedModel(graphName)) db.removeNamedModel(graphName)
//    db.addNamedModel(graphName,model,false)
//    db.commit()
//    db.close()
//    println(s"vos loaded $graphName")
//  }
//}
