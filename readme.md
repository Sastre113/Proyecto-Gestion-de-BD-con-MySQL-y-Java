# Proyecto Gestion de BD con MySQL y Java 

Autor: Miguel Ángel Sastre Gálvez
Nº Matrícula: y160374

El archivo diagnostico.java contiene todo el código del proyecto 1.
Como opcional se decía que se podía entregar un archivo pdf, en vez de eso, 
se ha agregado un comentario al inicio del archivo diagnostico.java.
Además, la gran mayoría de métodos están comentados.

# Documentation

## `static final String DRIVER = "com.mysql.jdbc.Driver"`

Variables de configuracion basicos para la base de datos. En un principio son fijos porque no hay necesidad de cambiar estos datos durante la ejecucion del programa

## `private final String db = "diagnostico"`

Nombre de la base de datos

## `private final String user = "bddx"`

Nombre de usuario

## `private final String pwd = "bddx_pwd"`

Contraseña de usuario

## `private final String db_url = "jdbc:mysql:`

Dirección url para conectar con la base de datos. Se trata de una dirección local.

## `private final String confTablas [] =`

Configuracion de las tablas y sus columnas.

## `private boolean dbExiste()`

Método auxiliar que comprueba si existe la base de datos con el nombre dado en this.db.

 * **Returns:** true existe la base de datos.

## `private void borrarBD()`

Método auxiliar para borrar la base de datos

## `private String[] crearTabla()`

Método auxiliar para crear las tablas de la base de datos.

 * **Returns:** Devuelve un array con el nombre de las tablas que se han creado 

     en la base de datos.

## `private void cargarBD (String nTablas []) throws SQLException`

Método auxiliar, encargado de extraer los datos del archivo disease_data.data y los organiza para finalmente insertarlos en la base de datos.

 * **Parameters:** `nTablas` — Array con cada una de las tablas existentes en la BD.
 * **Exceptions:** `SQLException` — 

## `private boolean insertar(int indice, LinkedList<String[]> listaDatos , String nomTabla)`

Método auxiliar para la insercion de los datos en la base de datos. Mediante un switch se controla en que tabla estamos y como se debe proceder para cargar los datos en ella.

----------------- Indice de nTablas -----------------

0. disease 1. source 2. symptom 3. code 4. disease_has_code 5. disease_symptom 6. sympstom_samantic_type

 * **Parameters:**
   * `int` — Controla en que tabla esa el proceso
   * `LinkedList<String[]>` — Contiene una lista con 

     los datos obtenidos de la linea
   * `String` — Recibe el nombre de la tabla en la que nos situamos,

## `private void cerrarStatement(Statement st,ResultSet rs,PreparedStatement pst)`

Método auxiliar para cerrar los Statement y ResultSet, que unicamente, se hayan abierto previamente.

 * **Parameters:**
   * `Statement` — 
   * `ResultSet` — 
   * `PreparedStatement` — 

## `private boolean vocaExists(String vocabulario)`

Método auxiliar que recibe como argumento un vocabulario y comprueba si existe en la base de datos

 * **Parameters:** `String` — Vocabulario buscado en la BD.

     
 * **Returns:** boolean Devuelve true si encuentra el vocabulario, ecc false.

## `private LinkedList<Integer> buscaEnfermedad(String [] arrayCui)`

Método auxiliar que recibe un array con los codigos asociados previamente descrimiandos. El método se asegurará de que los "Cui" son validos y no estan repetidos. Los cui se depuran y se filtran una vez más en este metodo para asegurar que son semenjantes al formato establecido en la base de datos. Se buscará la enfermedad más relacionada con los síntomas dados.

 * **Parameters:** `arrayCui` — Array con los codigos asociados a síntomas discriminados por "C",
 * **Returns:** LinkedList<Integer> Devuelve una lista del ID de las enfermedades que estan relacionadas

     con los codigos asociados dados por el usuario.

## `private LinkedList <Pair<String,Integer>> statsSinto(ResultSet rs, String orden) throws Exception`

Metodo auxiliar para mostrarEstadisticasBD(). Con el se obtiene una lista de pares de las enfermedades con mayor y menor cantidad de sintomas.

 * **Parameters:**
   * `rs` — Nos da el resultado del conteo para cada enfermedad.
   * `orden` — Orden seguido del ResultSet para ordenar el conteo.
 * **Returns:** LinkedList <Pair<String,Integer>> Lista de pares donde index = 0 nos dará las enfermedades que tengan más sintomas y index = 1 las de menos.
 * **Exceptions:** `Exception` — 

## `private int readInt() throws Exception`

Método para leer números enteros de teclado.

 * **Returns:** Devuelve el número leído.
 * **Exceptions:** `Exception` — Puede lanzar excepción.

## `private String readString() throws Exception`

Método para leer cadenas de teclado.

 * **Returns:** Devuelve la cadena leída.
 * **Exceptions:** `Exception` — Puede lanzar excepción.

## `private LinkedList<String> readData() throws Exception`

Método para leer el fichero que contiene los datos.

 * **Returns:** Devuelve una lista de String con el contenido.
 * **Exceptions:** `Exception` — Puede lanzar excepción.

## `private static Connection getConexion()`

Método getter para conexion.

 * **Returns:** Devuelve el atributo de tipo Connection

## `private static void setConexion(Connection conexion)`

Método setter para conexion.

 * **Parameters:** `conexion` — 

## `class Pair<K, V>`

 * **Author:** * <p>
 * **Version:** 1/5/2018

     <p>

     Clase auxiliar usada en mostrarEstadisticas().

     Tiene como objetivo colocar a la izquierda un String que será una enfermedad

     y la derecha el recuento de sintomas de esas enfermedad.

     <p>

     La clase sigue la lógica de un HashMap en cuanto a parejas de pares, donde

     enfermedad será la "Key" y "Value" será el recuento. El motivo de no usar

     hashmap es que una vez obtenida la lista de enfermades con más/menos síntomas

     al devolverla, no tenemos idea de cuales son para acceder a ellas mediante su "Key".

     <p>
 * **Parameters:**
   * `Atributo` — abstracto.
   * `Atributo` — abstracto.
