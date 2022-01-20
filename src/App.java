import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;



public class App {
    public static Scanner input = new Scanner(System.in);
    private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private static final int CURRENT_YEAR = cal.get(Calendar.YEAR);
    private static final int CURRENT_MONTH = cal.get(Calendar.MONTH)+1; //STARTS COUNTING ON 0 = JAN
    //private static final String[] ATR_DISC_VALUES = {"P","NP"};


    public static void main(String[] args) throws SQLException {
        optionsMenu();

        //queries.test();

    }

    private static void optionsMenu() throws SQLException {
        optionsMenuDisplay();
        switch (getValInt()) {
            case 1 -> novoActivo();
            case 2 -> substituirElem();
            case 3 -> activoForaServico();
            /*case 4 -> queries.custoTotalActivo();
            case 5 -> queries.query2d();
            case 6 -> queries.query2e();*/
            case 7 -> queries.query3c();
            //case 8 -> queries.query3d();
            case 9 -> exit();
            default -> System.err.println("Opção não reconhecida");
        }
    }


    private static void novoActivo() throws SQLException {
        System.out.println("Inserir novo Activo na Base de dados");
        System.out.println("Insira os seguintes dados relativos a um novo Activo");
        System.out.println("Nome");
        String nome = getValString();
        System.out.println("Data Aquisição");
        String data = getDate();
        System.out.println("Marca");
        System.out.println("Se o Activo não tiver marca escreva null");
        String marca = getValString();
        System.out.println("Modelo");
        System.out.println("Se o Activo não tiver modelo escreva null");
        String modelo = getValString();
        System.out.println("Localização");
        String local = getValString();
        //missing restriction check
        System.out.println("Id Activo Topo");
        System.out.println("Tem de corresponder a um dos Activos já presentes no sistema");
        System.out.println("Ids de activos no sistema: ");
        List<String> idActivos = queries.getIdActivos();
        printList(idActivos);
        String idactivotp = checkIfInList(idActivos);
        System.out.println("Tipo");
        List<Integer> tipos = queries.getIdTipos();
        printListInt(tipos);
        int tipo = checkIfInListInt(tipos);
        System.out.println("Empresa");
        System.out.println("Apenas são aceites empresas já presentes no sistema");
        List<String> empresas = queries.getEmpresas();
        printList(empresas);
        String nomeEmpresa = checkIfInList(empresas);
        int empresa = queries.getIdEmpresa(nomeEmpresa);
        System.out.println("Pessoa");
        List<String> nomePessoas = queries.getNomePessoas();
        printList(nomePessoas);
        String nomePessoa = checkIfInList(nomePessoas);
        int pessoa = queries.getIdPessoa(nomePessoa);

        queries.novoActivo(nome, data, marca, modelo, local, idactivotp, tipo, empresa, pessoa);
    }

    private static void substituirElem() throws SQLException {
        queries.substituirElem(1,2);
    }

    private static void activoForaServico() throws SQLException {
        //queries.activoForaServico();
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
        if(numRows == 1) System.out.println("Não existem valores para a interrogação efetuada");
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

    //exit app
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

    private static void printList(List<String> list) {
        for (String s : list) { System.out.print(s + "   "); }
        System.out.println();
    }

    private static void printListInt(List<Integer> list) {
        for (int s : list) { System.out.print(s + "   "); }
        System.out.println();
    }

    private static String getCurrentDateAndTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static String getDate(){
        System.out.println("Ano");
        int year = checkBetweenBoundaries(1950,2100);
        System.out.println("Mês");
        System.out.println("Entre 1 e 12");
        System.out.println("1 = JAN  12 = DEZ");
        int month;
        if(year == CURRENT_YEAR ) month = checkBetweenBoundaries(CURRENT_MONTH, 12);
        else month = checkBetweenBoundaries(1, 12);
        System.out.println("Dia");
        int lastdaymonth = lastDayMonth(month, year);
        System.out.println("Entre 1 e " + lastdaymonth);
        int day = checkBetweenBoundaries(1, lastdaymonth);
        return getStringDate(year,month,day);
    }

    private static String getTime(){
        System.out.println("Hora");
        int hour = checkBetweenBoundaries(0,23);
        System.out.println("Minutos");
        int minutes = checkBetweenBoundaries(0,59); // não se justifica colocar segundos
        return getStringTime(hour,minutes);
    }

    public static int lastDayMonth(int month, int year){
        int lastday = -1;
        int[] lastdayarray = {-1, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if(month == 2) {
            Calendar testcal = Calendar.getInstance();
            testcal.set(Calendar.YEAR, year);
            if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) lastday = 29;
            else lastday = 28;
        }
        if(month != 2) lastday = lastdayarray[month];
        return lastday;
    }

    public static String getStringDate(int year, int month, int day, int hour, int minutes){
        String date = year + "-";
        if(checkIfBelowMax(month,10)) date += month + "-";
        else date += "0" + month + "-";
        if(checkIfBelowMax(day,10)) date += day + " ";
        else date += "0" + day + " ";
        if(checkIfBelowMax(hour,10)) date += hour + ":";
        else date += "0" + hour + ":";
        if(checkIfBelowMax(minutes,10)) date += minutes;
        else date += "0" + minutes;
        date += ":00";
        return date;
    }

    public static String getStringDate(int year, int month, int day){
        String date = year + "-";
        if(checkIfBelowMax(month,10)) date += month + "-";
        else date += "0" + month + "-";
        if(checkIfBelowMax(day,10)) date += day;
        else date += "0" + day;
        return date;
    }

    public static String getStringTime(int hour, int minutes){
        String date = "";
        if(checkIfBelowMax(hour,10)) date += hour + ":";
        else date += "0" + hour + ":";
        if(checkIfBelowMax(minutes,10)) date += minutes;
        else date += "0" + minutes;
        date += ":00";
        return date;
    }

    public static int checkBetweenBoundaries(int min, int max) {
        int var;
        do{
            var = getValInt();
        }while(var < min || var > max);
        return var;
    }

    //public static boolean checkBetweenBoundaries(int var, int min, int max) {return (var < min || var > max);}

    public static int checkIfAboveMin(int min) {
        int var;
        do{
            var = getValInt();
        }while(var < min);
        return var;
    }

    public static int checkIfBelowMax(int max) {
        int var;
        do{
            var = getValInt();
        }while(var > max);
        return var;
    }

    public static boolean checkIfBelowMax(int var, int max) {return var >= max;}

    public static String[] listToArrayString(List<String> list){
        String[] array = new String[list.size()];
        for (int n=0; n<list.size(); n++){
            array[n] = list.get(n);
        }
        return array;
    }

    public static int[] listToArrayInt(List<Integer> list){
        int[] array = new int[list.size()];
        for (int n=0; n<list.size(); n++){
            array[n] = list.get(n);
        }
        return array;
    }

    public static String checkIfInArray(String[] array){
        String var;
        do{
            var = getValString();
        }while (!checkIfInArray(var, array));
        return var;
    }

    public static int checkIfInArrayInt(int[] array){
        int var;
        do{
            var = getValInt();
        }while (!checkIfInArrayInt(var, array));
        return var;
    }

    public static boolean checkIfInArray(String var, String[] array){
        for (String s : array) if (s.equals(var)) return true;
        return false;
    }

    public static boolean checkIfInArrayInt(int var, int[] array){
        for (int s : array) if (s==var) return true;
        return false;
    }

    public static String checkIfInList(List<String> list){
        String var;
        do{var = getValString();
        }while (!list.contains(var));
        return var;
    }

    public static int checkIfInListInt(List<Integer> list){
        int var;
        do{var = getValInt();
        }while (!list.contains(var));
        return var;    }

}
