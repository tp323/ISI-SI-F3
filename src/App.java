import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class App {
    public static Scanner input = new Scanner(System.in);
    private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private static final int CURRENT_YEAR = cal.get(Calendar.YEAR);
    private static final int CURRENT_MONTH = cal.get(Calendar.MONTH)+1; //STARTS COUNTING ON 0 = JAN
    //private static final String[] ATR_DISC_VALUES = {"P","NP"};




    public static void main(String[] args) throws SQLException {
        //test connection
        //queries.exampleTest();

        //optionsMenuDisplay();
        //optionsMenu();

        queries.test();


    }

    private static void optionsMenu() throws SQLException {
        optionsMenuDisplay();
        switch (getValInt()) {
            case 1 -> novoActivo();
            case 2 -> substituirElem();
            case 3 -> queries.activoForaServico();
            case 4 -> queries.custoTotalActivo();
            case 5 -> queries.query2d();
            case 6 -> queries.query2e();
            case 7 -> queries.query3c();
            case 8 -> queries.query3d();
            case 9 -> exit();
            default -> System.err.println("Opção não reconhecida");
        }
    }



    private static void novoActivo() throws SQLException {
        //String nome, Boolean estado, Date dt, String marca, String modelo, String local, String idactivotp, int tipo, int empresa, int pessoa

        System.out.println("Inserir novo Activo na Base de dados");
        System.out.println("Insira os seguintes dados relativos a um novo Activo");
        String nome = getValString("Nome");
        int estadoBol = getValInt("Estado");
        boolean estado = estadoBol == 1;
        String data = getDate("Data Aquisição");
        String marca = getValString("Marca");
        String modelo = getValString("Modelo");
        String local = getValString("Localização");
        String idactivotp = getValString("Id Activo Topo");
        int tipo = getValInt("Tipo");
        int empresa = getValInt("empresa");
        int pessoa = getValInt("pessoa");

        getValInt("");
        queries.novoActivo(nome, estado, data, marca, modelo, local, idactivotp, tipo, empresa, pessoa);
    }

    private static void substituirElem() throws SQLException {
        queries.substituirElem(1,2);
    }

    private static void optionsMenuDisplay() {
        System.out.println("Gestão de manutenção de activos físicos");
        System.out.println("1. Novo Activo (a)");
        System.out.println("2. Substituir um elemento de equipa (b)");
        System.out.println("3. Colocar Activo Fora de Serviço (c)");
        System.out.println("4. Custo Total Activo (d)");
        System.out.println("5. Pessoas que estão a realizar a intervenção na “válvula de ar condicionado“ " +
                "ou que gerem esse activo (2d)");
        System.out.println("6. Activos geridos ou intervencionados por “Manuel Fernandes” (2e)");
        System.out.println("7. Responsáveis de equipa que são (ou foram) gestores de pelo menos um activo (3c)");
        System.out.println("8. Intervenções programadas para daqui a um mês (3d)");
        System.out.println("9. Sair");
    }

    private static void checkRestrictions() throws SQLException{
        correctDBerrors();
        //todas as equipas têm no mínimo 2 pessoas
        queries.checkEquipasMin2Elements();
        //VCOMERCIAL.dtvcomercial de VCOMERCIAL >= ACTIVO.dtaquisicao
        queries.checkRestrictionVcomercial();
        //Se INTERVENCAO.valcusto > valor comercial do activo,
        // ACTIVO.estado = “0”, INTERVENCAO.estado = “concluído” e INTERVENCAO.dtfim = data actual
        queries.checkRestrictionIntervencao();
        //Se INTERVENCAO.dtfim é não nulo, estado = “concluído”;
        queries.checkRestrictionIntervencaoDtFim();
        //os activos “filhos” são do mesmo tipo que o activo “pai”;
        queries.checkRestrictionActivohierarchy();
        //a pessoa que gere um activo não faz a manutenção desse activo.
        queries.checkRestrictionConflictGestaoMan();
    }

    private static void correctDBerrors() throws SQLException{

    }


    public static void printTable(ResultSet rs, int columnsNumber) throws SQLException {
        int numRows = 1;
        while (rs.next()) {
            for(int i = 1 ; i <= columnsNumber; i++){
                if(i==1) System.out.print(numRows + " >  ");
                System.out.print(rs.getString(i) + "   "); //Print one element of a row
            }System.out.println();//Move to the next line to print the next row.
            numRows++;
        }
        if(numRows == 1) System.out.println("Não existem valores para a interrogação feitas");
    }

    private static int getValInt(String inputInstructions){
        System.out.println(inputInstructions);
        System.out.print("> ");
        int val = input.nextInt();
        //consume rest of line
        input.nextLine();
        return val;
    }

    private static String getValString(String inputInstructions){
        System.out.println(inputInstructions);
        System.out.print("> ");
        return input.nextLine();
    }

    private static int getValInt(){
        System.out.print("> ");
        int val = input.nextInt();
        //consume rest of line
        input.nextLine();
        return val;
    }

    private static String getValString(){
        System.out.print("> ");
        return input.nextLine();
    }

    private static String getDate(String inputInstructions) {
        System.out.println(inputInstructions);
        int ano = getValInt("Ano");
        if (ano < 1900 || ano > 2022) {}

        int mes = getValInt("Mês");
        int dia = getValInt("Dia");
        return "" + ano + "-" + mes + "-" + dia;
    }

    private static void exit() throws SQLException {
        System.out.println("Confirma Saída do Programa");
        if(checkConsent()) System.exit(0);
        else optionsMenu();
    }

    private static boolean checkConsent() {
        System.out.println("prima S para confirmar ou qualquer outra tecla para cancelar");
        char confirmExit = input.next().charAt(0);
        input.nextLine();
        return (confirmExit =='s' || confirmExit =='S');
    }


}
