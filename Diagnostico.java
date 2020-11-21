import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;


/**
 * 
 *	@author Miguel Ángel Sastre Gálvez,
 * 			Nº Matricula: y160374
 *	@version 11/05/2018 , 20:00
 */


/*	---------------------
 *  Comentario del autor
 *  ---------------------
 *  
 *  Cada método esta documentado y explicado de tal manera que puede generarse un javadoc del codigo.
 *  Este javadoc y sus comentarios pueden no estar depurados por la falta de tiempo y por dar prioridad al 
 *  buen funcionamiento del programa.
 *  
 *  Con el código se adjunta una imagen que muestra el directorio raíz de todo el proyecto, por si hubiera algún
 *  problema con las direcciones. 
 *  
 *  El tratamiento de las excepciones y el uso de try{}catch{} en mi opinión pueden que no estén demasiado depurados, pero
 *  en general, todos deberían dar una buena respuesta a errores comunes como:
 *  
 *  	1. Intentar hacer consultas sin que la base de datos este creada
 *  	2. Introducir valores erróneos.
 *  	3. No elegir bien la opción en los menús del programa.
 *  Entre muchas otras.
 *  
 *  Se ha usado una clase auxiliar, Pair<K,V> para el método mostrarEstadisticasBD();, esta clase se encuentra al final de
 *  este archivo. Es similar a la suministrada en la asignatura de Programación II y AED ( Y también alguna que hay por internet), 
 *  pero están suprimidos algunos métodos y cambiadas ciertas cosas innecesarias para este proyecto.
 *  
 *  El uso de la clase Pair se debe al hecho de tener objeto similar a HashMap, pero del que seamos capaz de obtener ambos valores
 *  que almacena, además, del hecho de no tener que conocer la "key" para obtener el valor relacionado con la clave.
 *  
 *  Por ultimo, un comentario sobre el apartado 3.b que tiene el siguiente enunciado.
 *  
 *  Listado de enfermedades de la base de datos y sus códigos asociados [0.5 puntos]: 
 *  El programa debe mostrar las enfermedades que contiene la base de datos, y 
 *  para cada enfermedad que códigos tiene (y tipo de código).
 *  
 *  He decidido implementarlo con el estilo del apartado 3.a, es decir, con un menú, para mostrar los datos de una manera
 *  más clara, exceptuando eso, hace lo mismo que si se mostraran todas las enfermedades, sus códigos y los vocabularios de los códigos.
 *  
 *  
 *	En el método "insertar" se debe evitar el uso "st.executeQuery("SELECT * FROM disease WHERE name=\"" + enfermedad[0] + "\"");"
 *	debido al posible uso malisioso de "sql injection"
 *
 */

public class Diagnostico {


	
	/**
	 *	Variables de configuracion basicos para la base de datos.
	 *	En un principio son fijos porque no hay necesidad de cambiar
	 *	estos datos durante la ejecucion del programa
	 */
	static final String DRIVER = "com.mysql.jdbc.Driver";	
	private final String DATAFILE = "data/disease_data.data";
	private static Connection conexion = null;
	
	/**
	 *  Nombre de la base de datos
	 */
	private final String db = "diagnostico";
	
	/**
	 *  Nombre de usuario
	 */
	private final String user = "bddx";
	
	/**
	 *  Contraseña de usuario
	 */
	private final String pwd = "bddx_pwd";
	
	/**
	 *  Dirección url para conectar con la base de datos.
	 *  Se trata de una dirección local.
	 */
	private final String db_url = "jdbc:mysql://localhost:3306/";
	
	/**
	 *  Configuracion de las tablas y sus columnas.
	 */
	private final String confTablas [] = {"disease:(disease_id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255), PRIMARY KEY(disease_id));",
			
			"source:(source_id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255), PRIMARY KEY(source_id));",
			
			"symptom:(cui VARCHAR(25) NOT NULL, name VARCHAR(255), semantic_type VARCHAR(255), PRIMARY KEY(cui));",

			"code:(code VARCHAR(255) NOT NULL, source_id INT, PRIMARY KEY(code,source_id),"
			+ "FOREIGN KEY (source_id) REFERENCES diagnostico.source(source_id));",
			
			"disease_has_code:(disease_id INT,code VARCHAR(255),source_id INT, PRIMARY KEY(disease_id,code,source_id)," 
            + "FOREIGN KEY (disease_id) REFERENCES disease(disease_id)," 
            + "FOREIGN KEY (code,source_id) REFERENCES code(code,source_id));",
            
			"disease_symptom:(disease_id INT, cui VARCHAR(25), PRIMARY KEY(disease_id,cui),"
			+"FOREIGN KEY (disease_id) REFERENCES disease(disease_id)," 
            +"FOREIGN KEY (cui) REFERENCES symptom(cui));"};

	private void showMenu(){

		int option = -1;
		do {
			System.out.println("Bienvenido a sistema de diagnóstico\n");
			System.out.println("Selecciona una opción:\n");
			System.out.println("\t1. Creación de base de datos y carga de datos.");
			System.out.println("\t2. Realizar diagnóstico.");
			System.out.println("\t3. Listar síntomas de una enfermedad.");
			System.out.println("\t4. Listar enfermedades y sus códigos asociados.");
			System.out.println("\t5. Listar síntomas existentes en la BD y su tipo semántico.");
			System.out.println("\t6. Mostrar estadísticas de la base de datos.");
			System.out.println("\t7. Salir.");
			try {
				option = readInt();
				switch (option) {
				case 1:
					crearBD();
					break;
				case 2:
					realizarDiagnostico();
					break;
				case 3:
					listarSintomasEnfermedad();
					break;
				case 4:
					listarEnfermedadesYCodigosAsociados();
					break;
				case 5:
					listarSintomasYTiposSemanticos();
					break;
				case 6:
					mostrarEstadisticasBD();
					break;
				case 7:
					exit();
				default:
					System.err.println("Opción introducida no válida!");
					break;
				}
			} catch (Exception e) {
			}
		} while (option != 7);
		exit();
	}

	private void exit() {
		System.out.println("Saliendo.. ¡hasta otra!");
		try {
			getConexion().close();
		} catch (SQLException e) { e.printStackTrace(); }
		System.exit(0);
	}

	// Como crear usuario y darle privilegios en SQL
	// CREATE USER 'bddx'@'localhost' IDENTIFIED BY 'bddx_pwd';
	// GRANT ALL PRIVILEGES on diagnostico.* to 'bddx'@'localhost';

	// Permitir concatenacion de "queries"
	// ?allowMultiQueries=true
	

	/**
	 *  Método principal para conectarse a la base de datos.
	 */
	
	//1.a
	private void conectar(){
		try{
				Class.forName(DRIVER);
				setConexion(DriverManager.getConnection(db_url + "?allowMultiQueries=true",this.user,this.pwd));
				if(dbExiste()){
					Statement st = conexion.createStatement();
					st.executeQuery("USE " + this.db);
				} 
		}catch(SQLException e){ System.err.println("La base de datos no existe, debe crearse primero.\n"); }
		catch(Exception e){
			System.err.println("Error al intentar conectar con la base de datos\n");
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Método auxiliar que comprueba si existe la base de datos 
	 *  con el nombre dado en this.db.
	 * 
	 * @return true existe la base de datos.
	 */
	
	private boolean dbExiste() {
        boolean existe = false;
        try {
            Connection conn = null;
            Statement st = null;
            Class.forName(DRIVER);
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" , this.user, this.pwd);
            st = conn.createStatement();
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + this.db + "'";
            ResultSet rs = st.executeQuery(sql);
            
            if (rs.next()) existe = true;
            
            st.close();
            conn.close();
        } catch (ClassNotFoundException ex) { } 
        catch (SQLException ex) { existe = false; }
        
        
        return existe;
    }
	
	/**
	 *  Método principal para crear la base de batos por completo.
	 *  Empezando por crear las tablas y su relacion
	 *  Y finalizando por la insercion de los datos obtenidos del
	 *  archivo disease_data.data
	 */
	
	//1.b
	private void crearBD() {		
		if(!dbExiste()){
			try {
				conectar();
				getConexion().setAutoCommit(false);
				
				String nTablas [];
				nTablas = crearTabla();
				cargarBD(nTablas);
				
				getConexion().commit();
				getConexion().setAutoCommit(true);
				System.out.println("¡¡ La base de datos se ha creado con exito !!\n");
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Error al crear la base de datos\n");
			}
		}else{
			System.out.println("¡Ya existe la base de datos!");
			System.out.println("¿Quiere borrar la base de datos existente?");
			System.out.println("Escriba \"Si\" para borrar o \"No\" para dejar la base de datos existente:\n");
			
			String opcion = "";
			boolean salir = false;
			do{
				try {
				 opcion = readString();
				} catch (Exception e){ }
				
				 if(opcion.compareToIgnoreCase("no") == 0){
					 salir = true;
					 System.out.println();
				 }
				 else if (opcion.compareToIgnoreCase("si") == 0){
					 borrarBD();
					 salir = true;
				 }else{
					 System.out.println("Por favor, debe escribir \"si\" o \"no\".");
				 }
						 
			}while(!salir);	
		
		}
	}
	
	/**
	 * Método auxiliar para borrar la base de datos
	 */
	
	private void borrarBD(){
		if(getConexion() == null) conectar();
		
		try{
			String query = "DROP DATABASE IF EXISTS " + this.db;
			PreparedStatement pst = getConexion().prepareStatement(query);
			pst.executeUpdate();
			pst.close();
			System.out.println("¡Base de datos borrada con exito!\n");
		}catch(Exception e){
			System.err.print("No se puede borrar la base de datos\n\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Método auxiliar para crear las tablas de la base de datos.
	 * 
	 * @return Devuelve un array con el nombre de las tablas que se han creado 
	 * 			en la base de datos.
	 */
	
	private String[] crearTabla(){
		
		String crearTabla = "CREATE TABLE ";
		String queryCleaner = "DROP TABLE IF EXISTS ";
		String nTablas [] = new String [confTablas.length];
		String query = "CREATE DATABASE " + this.db;
		
		try {
			// Si existe una BD con el mismo nombre, se borra y se vuelve a crear desde 0
			Statement st = getConexion().createStatement();
			st.executeUpdate(query);
			st.executeQuery("USE " + this.db);
			st.close();
			
			PreparedStatement pst = null;
			for (int i = 0; i < confTablas.length; i++) {
			    query = crearTabla;
				String parts[] = confTablas[i].split(":");
				nTablas[i] = parts[0];
				query = query + parts[0]+ " " + parts[1];
				
				pst = getConexion().prepareStatement(queryCleaner + parts[0] +"; "+ query);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println("Fallo en la creación de la base de datos");
			e.printStackTrace();
		}	
		return nTablas;
	}
	
	
	/**
	 *  Método auxiliar, encargado de extraer los datos del archivo disease_data.data
	 *  y los organiza para finalmente insertarlos en la base de datos.
	 *  
	 * @param nTablas Array con cada una de las tablas existentes en la BD.
	 * @throws SQLException 
	 */
	
	private void cargarBD (String nTablas []) throws SQLException{
		try {
			LinkedList<String> datos = readData();
			Iterator<String> it = datos.iterator();
			
			while(it.hasNext()){
				/*
				 *  Procedimiento que divide cada dato y se reparte en diferentes variables.
				 *  Esquema de como vienen los datos en disease_data.data
				 *  enfermedad:codigo1@vocabulario1;....;codigoN@vocabularioN=Sintoma1:codigoSintoma1:semanticTypeSintoma1;...
				 *  ..;SintomaN:codigoSintomaN:semanticTypeSintomaN
				 *  
				 */
				
				String linea = it.next();
				String parts[] = linea.split("=");
				String enferCodVoc [] = parts[0].split(":");
				// Enfermedad
				String enfermedad[] = new String[1];
				enfermedad[0] = enferCodVoc[0];
				// Tupla de 2 datos = enferCodVoc[1] = codigo1@vocabulario1; ... ;codigoN@vocabularioN ;
				String CodVoc [] = enferCodVoc[1].split(";");
				// Tupla de 3 datos = Sintoma1:codigoSintoma1:semanticTypeSintoma1;...;
				String sintCodSemantic [] = parts[1].split(";");
				
				// Todos los datos organizados en array's				
				String codigos[] = new String [CodVoc.length];
				String vocabularios[] = new String [CodVoc.length];
				
				String sintoma[] = new String [sintCodSemantic.length];
				String codigoSint[] = new String [sintCodSemantic.length];
				String semanticType[] = new String [sintCodSemantic.length];

				
				/*
				 *  El siguiente bucle reparte los datos en diferentes arrays
				 *  Codigos --> Tiene todos los codigos en esta linea 
				 *  Vocabularios --> tiene todos los vocabularios en esta linea
				 *  
				 *  El indice de un codigo corresponde con el del vocabulario
				 */
				
				int itera = 0;
				if(codigos.length > sintCodSemantic.length) itera = codigos.length ;
				else itera = sintCodSemantic.length;
				
				for(int i = 0; i < itera; i++){
					if( i < codigos.length){
						String CodigoYVoca[] = CodVoc[i].split("@");
						codigos[i] = CodigoYVoca[0];
						vocabularios[i] = CodigoYVoca[1];
					}
					
					if(i < sintCodSemantic.length){
						String sintCod[] = sintCodSemantic[i].split(":"); 
						sintoma[i] = sintCod[0];
						codigoSint[i] = sintCod[1];
						semanticType[i] = sintCod[2];
					}	
				}

				
				
// 				Esquema con todos los datos organizados en array's:
				
//					String enfermedad[]
//					String codigos[]  
//					String vocabularios[] 
				
//					String sintoma[] 
//					String codigoSint[]
//					String semanticType[]
//					int cantDatos = (CodVoc.length * 2) + (sintCodSemantic.length * 3);
				
				LinkedList<String[]> listaDatos = new LinkedList<String[]>();
				
				listaDatos.add(enfermedad); 	// 0, disease
				listaDatos.add(vocabularios);	// 1, source
				listaDatos.add(codigos);  		// 2, code
				listaDatos.add(codigoSint); 	// 3, cui
				listaDatos.add(sintoma); 		// 4
				listaDatos.add(semanticType);	// 5
				
				
//				String nTablas [] --> Contiene el nombre de todas las tablas en la BD
				boolean estado = true;
				for(int i = 0; i < nTablas.length && estado; i++){
					estado = insertar(i,listaDatos,nTablas[i]);
				}
				
				if(estado == false){
					throw new Exception();
				}
			}// FIN while(it.hasNext())
		}catch (Exception e) {
			System.err.println("Fallo en la carga de los datos.\nSe procede al borrado de la base de datos.");
			borrarBD();	
			e.printStackTrace();
		}

	}
	
	/**
	 * 	Método auxiliar para la insercion de los datos en la base de datos.
	 *  Mediante un switch se controla en que tabla estamos y como se debe proceder
	 *  para cargar los datos en ella.
	 *  
	 *  -----------------
	 *  Indice de nTablas
	 *  -----------------
	 *  
	 *  0. disease
	 *  1. source
	 *  2. symptom
	 *  3. code
	 *  4. disease_has_code
	 *  5. disease_symptom
	 *  6. sympstom_samantic_type
	 *  
	 *  @param int Controla en que tabla esa el proceso
	 *  @param LinkedList<String[]> Contiene una lista con 
	 *  								los datos obtenidos de la linea
	 *  @param String Recibe el nombre de la tabla en la que nos situamos,
	 */
	
	private boolean insertar(int indice, LinkedList<String[]> listaDatos , String nomTabla){
		
		// Variables auxiliares para ayudar a la lectura del codigo
		String enfermedad [];
		String codigo [];
		String vocabularios [];
		String cui [];
		String sintoma [];
		String semantic [];
		int  disease_id;
		
		String query = "INSERT INTO " + nomTabla;
		int size = 0;
		Statement st = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try{
			switch(indice){
			// tabla disease
			case 0:
				enfermedad = listaDatos.get(0);
				query = query + " (name) VALUES (?)";
				pst = getConexion().prepareStatement(query);
				pst.setString(1, enfermedad[0]);
				pst.executeUpdate();
				break;
			// tabla source
			case 1:
				vocabularios = listaDatos.get(1);
				query = query + " (name) VALUES (?)";
				pst = getConexion().prepareStatement(query);
				for(String vocabulario: vocabularios){
					if(!vocaExists(vocabulario)){
						pst.setString(1, vocabulario);
						pst.executeUpdate();
					}
				}
				break;
			// tabla symptom
			case 2:
				query =  query + " (cui,name,semantic_type) VALUES (?,?,?);";
				pst = getConexion().prepareStatement(query);
				st = getConexion().createStatement();
				cui  = listaDatos.get(3);
				sintoma = listaDatos.get(4);
				semantic = listaDatos.get(5);
				size = cui.length;
				
				for(int i = 0; i < size; i++){
					rs = st.executeQuery("SELECT cui FROM symptom WHERE cui=\"" + cui[i]+"\";");
					
					if(!rs.next()){
						pst.setString(1, cui[i]);
						pst.setString(2, sintoma[i]);
						pst.setString(3, semantic[i]);
						pst.executeUpdate();
					}
				}		
				break;
			// tabla code
			case 3:
			    vocabularios = listaDatos.get(1);
			    codigo = listaDatos.get(2);
			    size = codigo.length;
				
				query = query + " (code,source_id) VALUES (?,?);";
				pst = getConexion().prepareStatement(query);
				st = getConexion().createStatement();
				
				for(int i = 0; i < size; i++){
					rs = st.executeQuery("SELECT * FROM source WHERE name=\"" + vocabularios[i] + "\"");
					rs.next();

					pst.setString(1,codigo[i]);
					pst.setString(2,rs.getString(1));
					pst.executeUpdate();
				}
				break;
			// tabla disease_has_code
			case 4:
				enfermedad = listaDatos.get(0);
				codigo = listaDatos.get(2);
				query = query + " (disease_id,code,source_id) VALUES (?,?,?);";
				pst = getConexion().prepareStatement(query);
				
				st = getConexion().createStatement();
				rs = st.executeQuery("SELECT * FROM disease WHERE name=\"" + enfermedad[0] + "\"");
				rs.next();
				disease_id = rs.getInt(1);
				
				for(String cod: codigo){
					rs = st.executeQuery("SELECT * FROM code WHERE code=\"" + cod + "\"");
					rs.next();
					pst.setInt(1,disease_id);
					pst.setString(2,cod);
					pst.setInt(3, rs.getInt(2));
					pst.executeUpdate();
				}
				break;
		    // tabla disease_symptom
			case 5:
				enfermedad = listaDatos.get(0);
			    cui = listaDatos.get(3); 
				query = query + " (disease_id,cui) VALUES (?,?);"; 
				pst = getConexion().prepareStatement(query);
				st = getConexion().createStatement();
				rs = st.executeQuery("SELECT * FROM disease WHERE name=\"" + enfermedad[0] + "\"");
				rs.next();
				
				for(String cod: cui){
					pst.setInt(1,rs.getInt(1));
					pst.setString(2,cod);
					pst.executeUpdate();
				}
				break;
			}// Fin switch
			
			// Método que cierra aquellos Statement y ResultSet abiertos.
			cerrarStatement(st,rs,pst);
			return true;
		} catch (Exception e) {
				System.err.println("Error al insertar los datos en las tablas\n");
			return false;
		}
	}
	
	
	
	/**
	 * 	Método auxiliar para cerrar los Statement y ResultSet, que unicamente, se
	 *  hayan abierto previamente.
	 *  
	 *  @param Statement
	 *  @param ResultSet
	 *  @param PreparedStatement
	 */
	
	private void cerrarStatement(Statement st,ResultSet rs,PreparedStatement pst){
		try {
			if(st  != null) st.close();
			if(rs  != null) st.close();
			if(pst != null) pst.close();
		}catch (SQLException e){
			System.err.println("Hubo un error al cerrar los Statement\n");
			e.printStackTrace();
		}
	}
	
	/**
	 *	Método auxiliar que recibe como argumento un vocabulario y comprueba
	 *	si existe en la base de datos
	 *
	 *	@param String Vocabulario buscado en la BD.
	 *	
	 *	@return boolean Devuelve true si encuentra el vocabulario, ecc false.
	 */
	
	private boolean vocaExists(String vocabulario){
		  boolean exist = false;
	        try {
	            Statement st =  null;
	            st = getConexion().createStatement();
	            String sql = "SELECT name FROM source WHERE name=\""+ vocabulario +"\";";
	            ResultSet rs = st.executeQuery(sql);

	            if (rs.next()) {
	                exist = true;
	            }
	        } catch (SQLException ex) {
	            exist = false;
	        }
	        return exist;
	}

	//2.
	private void realizarDiagnostico() {

		if(getConexion() == null) conectar();
		
		try {
			LinkedList<Integer> listaIdEnfermedades = null;
			String query = "SELECT name,cui FROM symptom ORDER BY cui ASC;";

			PreparedStatement pst = getConexion().prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			int contRow = 0;
			
			while(rs.next()){
				System.out.print("| "+ rs.getString(1) + " - " + rs.getString(2) + " \t");
				contRow++;
				if(contRow == 2){
					contRow = 0;
					System.out.println("\r");
				}
			}
			

			int salir = -1;
			do{
				System.out.println("\n\nPara buscar las enfermedades relacionadas con los sintomas,"
						+ "\nintroduzca el codigo identificativo de los sintomas que produzca dicha enfermedad: \n");
				System.out.println("Puede pulsar \"ENTER\" para salir.");
				String sintBuscado = readString();
				sintBuscado = sintBuscado.toUpperCase();
				String arrayCui[] = sintBuscado.split("(?=C)");
				
				if(arrayCui.length == 1 && sintBuscado.charAt(0) == 'C')arrayCui[0] = arrayCui[0].substring(0, 8);

				for(int i = 0; i < arrayCui.length; i++){
					if(arrayCui[i].length() >= 8){
						arrayCui[i] = arrayCui[i].substring(0,8);
						salir = 0;
					}
				}
				
				if(salir == 0) listaIdEnfermedades = buscaEnfermedad(arrayCui);
				else if(sintBuscado.isEmpty())salir = 1;
				else{
					System.out.println("Opción introducida no valida!");
				}
			}while(salir == -1);
			

			if(listaIdEnfermedades != null){
				Iterator<Integer> it = listaIdEnfermedades.iterator();
					
				while(it.hasNext()){
					int disease_id = it.next();
					query = "SELECT name FROM disease WHERE disease_id=?;";
					pst.setInt(1,disease_id);
					pst = getConexion().prepareStatement(query);
					rs = pst.executeQuery();
					rs.next();
					System.out.println(rs.getString(1));
				}
				
			}else if(salir == 0)System.out.println("No se ha podido encontrar ninguna enfermedad con los síntomas dados.");
				
				
			System.out.println();
			pst.close();
			rs.close();
		}catch(SQLException e){ System.err.println("!No existe la base de datos!");}
		catch(Exception e){ e.printStackTrace();}
	}
	
	/**
	 * 	Método auxiliar que recibe un array con los codigos asociados previamente descrimiandos.
	 *  El método se asegurará de que los "Cui" son validos y no estan repetidos.
	 *  Los cui se depuran y se filtran una vez más en este metodo para asegurar que son semenjantes
	 *  al formato establecido en la base de datos.
	 *  Se buscará la enfermedad más relacionada con los síntomas dados.
	 *  
	 * @param arrayCui Array con los codigos asociados a síntomas discriminados por "C",
	 * @return LinkedList<Integer> Devuelve una lista del ID de las enfermedades que estan relacionadas
	 * 	con los codigos asociados dados por el usuario.
	 */
	
	private LinkedList<Integer> buscaEnfermedad(String [] arrayCui){
		String query = "SELECT disease_id,cui FROM disease_symptom WHERE cui=";
		LinkedList<String> listaCui = new LinkedList<String>();
		LinkedList<Integer> listaSalida = new LinkedList<Integer>();
		try{
			// Conexion auxiliar para usar tablas temporales.
			Connection conexion2 = DriverManager.getConnection(db_url + this.db + "?allowMultiQueries=true",this.user,this.pwd);

			PreparedStatement pst = null;
			ResultSet rs = null;
			
				for(String cui: arrayCui){
					if(cui.length() == 8){
						query = "SELECT disease_id,cui FROM disease_symptom WHERE cui=? ;";
						pst = conexion2.prepareStatement(query);
						pst.setString(1, cui);
						rs = pst.executeQuery();
					
						if(!rs.next()){
							System.out.println("No se ha encontrado coincidencia en "
									+ "la base de datos con " + cui);
						}else if(listaCui.indexOf(cui) == -1)listaCui.add(cui); 
					}
				}
				PreparedStatement pst2 = null; 
				ResultSet rs2 = null ;

				if(!listaCui.isEmpty() && listaCui.size() > 1){
					pst = conexion2.prepareStatement("CREATE TEMPORARY TABLE tabla_cui_referencia SELECT disease_id FROM disease_symptom WHERE cui=?;");
					pst.setString(1, listaCui.poll());
					pst.executeUpdate();
					
					Iterator<String> it = listaCui.iterator();
					query = "CREATE TEMPORARY TABLE tabla_cui_inter SELECT DISTINCT disease_id FROM disease_symptom WHERE cui=?;";
					while(it.hasNext()){
						pst = conexion2.prepareStatement(query);
						pst.setString(1, it.next());
						pst.executeUpdate();
						pst = conexion2.prepareStatement("SELECT  tabla_cui_referencia.disease_id FROM tabla_cui_referencia INNER JOIN "
								+ "tabla_cui_inter ON tabla_cui_referencia.disease_id = tabla_cui_inter.disease_id;");
						rs = pst.executeQuery();
						pst = conexion2.prepareStatement("DROP TEMPORARY TABLE tabla_cui_inter;");
						pst.executeUpdate();
					}	
				}else {
					pst = conexion2.prepareStatement("SELECT disease_id FROM disease_symptom WHERE cui=\"" + listaCui.poll() + "\";");
					rs = pst.executeQuery();
				}
			
			if(rs.first()){
				rs.beforeFirst();
				System.out.println("Enfermedades relacionadas con los síntomas: \n");
				while(rs.next()){
					
					pst2 = conexion2.prepareStatement("SELECT * FROM disease WHERE disease_id=? ;");
					pst2.setString(1, rs.getString(1));
					rs2 = pst2.executeQuery();
					rs2.next();
					System.out.println(rs2.getInt(1) + ". " + rs2.getString(2));			
				}
			}else System.out.println("No existe enfermedades relacionadas.\n");
			
			pst.close();
			rs.close();
			conexion2.close();
			}catch(SQLException e){
				e.printStackTrace(); 
		}
		return listaSalida;
	}
	
	//3.a
	private void listarSintomasEnfermedad() {
		
		if(getConexion() == null) conectar();
		
		try{
			String query = "SELECT * FROM disease;";
			PreparedStatement pst = getConexion().prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			
			LinkedList<String> listaEnfer = new LinkedList<String>();
			while(rs.next()){
				listaEnfer.add(rs.getString(2));	
			}
			
			 int opcion = -1;
			 int salir = listaEnfer.size() + 1;
			 
			 do{
				 for(int i = 0;  i < listaEnfer.size();i++){
						if(i == 0) System.out.println("Lista de enfermedades\n");
						System.out.println(i+1 + ". " + listaEnfer.get(i));
					}
				  
				 System.out.println("\n" + salir + ". Volver al menú principal\n");
				 System.out.println("Elija una opción mediante su ID: ");
				 
				 try{
					 opcion = readInt();
					 
					if(opcion > 0 && opcion < salir){
						int opcionElegida = listaEnfer.indexOf(listaEnfer.get(opcion - 1)) + 1;
						System.out.println("Enfermedad elegida: " + listaEnfer.get(opcion - 1) + "\n");
						System.out.println("Síntomas: ");
						query = "SELECT cui FROM disease_symptom WHERE disease_id=?";
						pst = getConexion().prepareStatement(query);
						pst.setInt(1, opcionElegida);
						rs = pst.executeQuery();
						
						int contador = 1;
						PreparedStatement pst2 = null;
						ResultSet rs2 = null;
						while(rs.next()){						
							String query2 = "SELECT name FROM symptom WHERE cui=?;";
							pst2 = getConexion().prepareStatement(query2);
							pst2.setString(1, rs.getString(1));
							rs2 = pst2.executeQuery();
							rs2.next();
							System.out.println(contador++ + ". " + rs2.getString(1));
						}
						pst2.close();
						rs2.close();	
					} else if(opcion ==  salir){
							// se sale del bucle
							System.out.println("Volviendo al menú principal...");
					} else System.err.println("Opción introducida no válida!");
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 
				 System.out.println();
			 }while(opcion != salir);
			 pst.close();
			 rs.close();
		}catch(SQLException e){
			System.err.println("!No existe la base de datos!");
		}catch(Exception e2){
			System.err.println("No se pudo mostrar los datos.");
		}
	}
	

	//3.b
	private void listarEnfermedadesYCodigosAsociados() {

		if (getConexion() == null) conectar();

		try {
			String query = "SELECT * FROM disease ORDER BY disease_id ASC;";
			PreparedStatement pst = getConexion().prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			LinkedList<String> listaEnfer = new LinkedList<String>();
			//listaEnfer.add("Lista de enfermedades\n");
			while (rs.next()) {
				listaEnfer.add(rs.getString(2));
			}

			int opcion = -1;
			int salir = listaEnfer.size() + 1;

			do {
				System.out.println("Lista de enfermedades\n");
				for (int i = 0; i < listaEnfer.size(); i++) {
					System.out.println(i + 1 + ". " + listaEnfer.get(i));
				}

				System.out.println("\n" + salir + ". Volver al menú principal\n");
				System.out.println("Elija una opción mediante un número: ");

				try {
					opcion = readInt();
					if(opcion > 0 && opcion < salir){
						int opcionElegida = listaEnfer.indexOf(listaEnfer.get(opcion-1));
						System.out.println("Enfermedad elegida: " + listaEnfer.get(opcionElegida));
						
						query = "SELECT code,source_id FROM disease_has_code WHERE disease_id=? ;";
						pst = getConexion().prepareStatement(query);
						pst.setInt(1, opcion);
						rs = pst.executeQuery();
						
						String query2 = "SELECT name FROM source WHERE source_id=?;";
						PreparedStatement pst2 = null;
						ResultSet rs2 = null;
						
						while(rs.next()){
							pst2 = getConexion().prepareStatement(query2);
							pst2.setInt(1, rs.getInt(2));
							rs2 = pst2.executeQuery();
							rs2.next();
							System.out.println(rs.getRow()+ ". Codigo: " + rs.getString(1)+ " - Vocabulario: " + rs2.getString(1));
						}
						pst2.close();
						rs2.close();
					}else if (opcion == salir) {
						// se sale del bucle
						System.out.println("Volviendo al menú principal...");
					}else System.err.println("Opción introducida no válida!");

				} catch (Exception e) {
					e.printStackTrace();
				}	
				System.out.println();
				pst.close();
				rs.close();
				
			} while (opcion != salir);
		} catch (SQLException e) {
			System.err.println("!No existe la base de datos!");
		}
	}
	
	//3.c
	private void listarSintomasYTiposSemanticos() throws Exception {

		if(getConexion() == null) conectar();
		
		try {
			String query = "SELECT name,semantic_type FROM diagnostico.symptom ORDER BY semantic_type DESC;";
			PreparedStatement pst = getConexion().prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			
			System.out.println("Síntoma - Semantic Type");
			System.out.println("-----------------------");
			while(rs.next()){
				System.out.println(rs.getRow() + ". " + rs.getString(1) + " - " + rs.getString(2));
			}
			System.out.println();
			pst.close();
			rs.close();
		} catch (SQLException e) {
			System.err.println("!No existe la base de datos!");
		} catch(Exception e2){
			throw new Exception("No se pudo mostrar los síntomas.");
		}
	}
	
	//4.
	private void mostrarEstadisticasBD() {
		if(getConexion() == null) conectar();
	
		try{
			String nomColum[] = {"name","cui"};
			String nomTablas[] = {"disease","symptom"};
			String datoBuscado[] = {"enfermedades","síntomas"};
			String query;
			String queryAvg;
			
			Statement st = getConexion().createStatement();
			ResultSet rs = null;
			int indice = 0;

			for(indice = 0 ; indice < nomTablas.length; indice++){
				query = "SELECT COUNT("+ nomColum[indice] +") FROM " + nomTablas[indice] +" ;" ;
				rs = st.executeQuery(query);
				rs.next();
				System.out.println(" Número de " + datoBuscado[indice] + " = " + rs.getInt(1) );
			}
			
			queryAvg = "SELECT avg(rcount) AS media FROM (SELECT COUNT(cui) AS rcount FROM disease_symptom r GROUP BY disease_id ) a ;";
			rs = st.executeQuery(queryAvg);
			rs.next();
			System.out.println(" Número medio de síntomas por enfermedad es: " + rs.getInt(1));
			System.out.println();
			
			LinkedList<LinkedList <Pair<String,Integer>>> lista = new LinkedList<LinkedList <Pair<String,Integer>>>();
			String orden [] = {"DESC","ASC"};
			
			for(indice = 0; indice < orden.length; indice++){
				rs = st.executeQuery("SELECT disease_id, COUNT(cui) FROM disease_symptom GROUP BY disease_id ORDER BY COUNT(cui) "+ orden[indice]
						+" ;" );
				lista.add(statsSinto(rs,orden[indice]));
			}
			
			/*
			 *  Con este bucle obtenemos la lista con las enfermedades con más y menos sintomas.
			 */
			for(indice = 0; indice < lista.size(); indice++){
				Iterator<?> it = lista.get(indice).iterator();
				while(it.hasNext()){
					Pair<String,Integer> par = (Pair<String, Integer>) it.next();
					
					if(indice == 0 && lista.get(indice).size() == 1){
						System.out.println("La enfermedad con más síntomas es:\n" +  par.getLeft() +" con " + par.getRight());
					}else if(indice == 0 && lista.get(indice).size() > 1){
						System.out.println("Las enfermedades con más síntomas son:" );
						System.out.println(par.getLeft() +" con " + par.getRight());
						while(it.hasNext()){
							par = (Pair<String, Integer>) it.next(); 
							System.out.println(par.getLeft() +" con " + par.getRight());
						}
					}
					System.out.println();
					if(indice == 1 && lista.get(indice).size() == 1){
						System.out.println("La enfermedad con menos síntomas es:\n " +  par.getLeft() +" con " + par.getRight());
					}else if(indice == 1 && lista.get(indice).size() > 1){
						System.out.println("Las enfermedades con menos síntomas son:" );
						System.out.println(par.getLeft() +" con " + par.getRight());
						while(it.hasNext()){
							par = (Pair<String, Integer>) it.next(); 
							System.out.println(par.getLeft() +" con " + par.getRight());
						}
					}
				}
			}
			
			System.out.println();
			System.out.println("Tipos de semantic type y con cuantos síntomas estan relacionados: ");
			rs = st.executeQuery("SELECT DISTINCT semantic_type FROM symptom ORDER BY semantic_type DESC;");
			Statement st2 = getConexion().createStatement();
			ResultSet rs2 = null;
			while(rs.next()){
				String semantic_type = rs.getString(1);
				System.out.print(semantic_type + " está relacionado con ");
				query = "SELECT COUNT(name) FROM symptom WHERE semantic_type=\"" + semantic_type + "\";";
				
				rs2 = st2.executeQuery(query);
				rs2.next();
				System.out.println(rs2.getInt(1) + " síntomas.");
			}
			
			System.out.println();
			st.close();
			st2.close();
			rs.close();
			rs2.close();
		}catch(SQLException e){
			System.err.println("!No existe la base de datos!");
		}
		catch(Exception e){
			System.err.println("No se pudo mostrar ninguna estadística\n");
		}
	}
	
	
	/**
	 *  Metodo auxiliar para mostrarEstadisticasBD(). Con el se obtiene una lista de pares de las enfermedades con mayor
	 *  y menor cantidad de sintomas. 
	 * @param rs  Nos da el resultado del conteo para cada enfermedad.
	 * @param orden Orden seguido del ResultSet para ordenar el conteo.
	 * @return  LinkedList <Pair<String,Integer>> Lista de pares donde index = 0 nos dará las enfermedades que tengan más sintomas y index = 1 las de menos.
	 * @throws Exception
	 */
	private LinkedList <Pair<String,Integer>> statsSinto(ResultSet rs, String orden) throws  Exception{
		try{
			int id = -1;
			LinkedList <Pair<String,Integer>> lista = new LinkedList<Pair<String,Integer>>();
			Pair <String,Integer> paresIdCount;
			Statement st = getConexion().createStatement();
			ResultSet rs2 = null;
			int aux = 0;
			String query = "SELECT name FROM disease WHERE disease_id=\"" ;
			boolean finWhile = false;
			switch(orden){
			case "DESC":
				int mayor = 0;
				while(!finWhile && rs.next()){
					aux = rs.getInt(2);
					if(mayor < aux){
						id = rs.getInt(1);
						mayor = aux;
						
						rs2 = st.executeQuery(query + id +"\"");
						rs2.next();
						lista.add(new Pair(rs2.getString(1),mayor));
					} else if(mayor == aux){
						id = rs.getInt(1);
						mayor = rs.getInt(2);
						
						rs2 = st.executeQuery(query + id +"\"");
						rs2.next();
						lista.add(new Pair(rs2.getString(1),mayor));
					}else finWhile = true;
				}
				break;
			case "ASC":
				rs.last();
				int menor = rs.getInt(2);
				rs.first();
				
				do{
					aux = rs.getInt(2);
					if(menor > aux){
						id = rs.getInt(1);
						menor = aux;
						
						rs2 = st.executeQuery(query + id +"\"");
						rs2.next();
						lista.add(new Pair(rs2.getString(1),menor));
					}else if(menor == aux){
						id = rs.getInt(1);
						menor = rs.getInt(2);
						
						rs2 = st.executeQuery(query + id +"\"");
						rs2.next();
						lista.add(new Pair(rs2.getString(1),menor));
					}else finWhile = true;
					
				}while(!finWhile && rs.next());
				break;
			}
			
			st.close();
			rs2.close();	
			return lista;
		}catch(Exception e){
			throw new Exception("Error al crear la lista de pares de mayor/menor cantidad de sintomas por enfermedad");
		}
		
	}
	
	
	/**
	 * Método para leer números enteros de teclado.
	 * 
	 * @return Devuelve el número leído.
	 * @throws Exception
	 *             Puede lanzar excepción.
	 */
	private int readInt() throws Exception {
		try {
			System.out.print("> ");
			return Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
		} catch (Exception e) {
			throw new Exception("Not number");
		}
	}

	/**
	 * Método para leer cadenas de teclado.
	 * 
	 * @return Devuelve la cadena leída.
	 * @throws Exception
	 *             Puede lanzar excepción.
	 */
	private String readString() throws Exception {
		try {
			System.out.print("> ");
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception e) {
			throw new Exception("Error reading line");
		}
	}

	/**
	 * Método para leer el fichero que contiene los datos.
	 * 
	 * @return Devuelve una lista de String con el contenido.
	 * @throws Exception
	 *             Puede lanzar excepción.
	 */
	private LinkedList<String> readData() throws Exception {
		LinkedList<String> data = new LinkedList<String>();
		BufferedReader bL = new BufferedReader(new FileReader(DATAFILE));
		while (bL.ready()) {
			data.add(bL.readLine());
		}
		bL.close();
		return data;
	}

	public static void main(String args[]) {
		new Diagnostico().showMenu();
	}

	/**
	 *  Método getter para conexion.
	 *  
	 * @return Devuelve el atributo de tipo Connection
	 */
	private static Connection getConexion() {
		return conexion;
	}

	/**
	 * 	Método setter para conexion.
	 * 
	 * @param conexion
	 */
	private static void setConexion(Connection conexion) {
		Diagnostico.conexion = conexion;
	}
}


/**
 * @author 
 * 
 * @version 1/5/2018
 * 
 * Clase auxiliar usada en mostrarEstadisticas().
 * Tiene como objetivo colocar a la izquierda un String que será una enfermedad
 * y la derecha el recuento de sintomas de esas enfermedad.
 * 
 * La clase sigue la lógica de un HashMap en cuanto a parejas de pares, donde
 * enfermedad será la "Key" y "Value" será el recuento. El motivo de no usar
 * hashmap es que una vez obtenida la lista de enfermades con más/menos síntomas
 * al devolverla, no tenemos idea de cuales son para acceder a ellas mediante su "Key".
 *
 * @param Atributo abstracto.
 * @param Atributo abstracto.
 */
class Pair<K, V> {

    private final K izquierda;
    private final V right;

    public Pair(K izquierda, V right) {
        this.izquierda = izquierda;
        this.right = right;
    }

    public K getLeft() {
        return izquierda;
    }

    public V getRight() {
        return right;
    }

}
