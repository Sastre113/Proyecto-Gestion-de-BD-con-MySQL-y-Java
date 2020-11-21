import java.io.InputStreamReader;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;

public class Diagnostico {

	private final String DATAFILE = "data/disease_data.data";

	private void showMenu() {

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
					break;
				}
			} catch (Exception e) {
				System.err.println("Opción introducida no válida!");
			}
		} while (option != 7);
		exit();
	}

	private void exit() {
		System.out.println("Saliendo.. ¡hasta otra!");
		System.exit(0);
	}

	private void conectar() {
		// implementar
	}

	private void crearBD() {
		// implementar
	}

	private void realizarDiagnostico() {
		// implementar
	}

	private void listarSintomasEnfermedad() {
		// implementar
	}

	private void listarEnfermedadesYCodigosAsociados() {
		// implementar
	}

	private void listarSintomasYTiposSemanticos() {
		// implementar
	}

	private void mostrarEstadisticasBD() {
		// implementar
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
}
