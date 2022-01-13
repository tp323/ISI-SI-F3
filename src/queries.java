import java.sql.*;

public class queries {
    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;

    private static int SIZE_NUMBER_PART_ID = 4;

    public static void test() throws SQLException {
        connect();
        String maxId = queries.maxIdActivo();
        closeConnection();
        System.out.println(maxId);

        String newId = increaseId(maxId);
        System.out.println(newId);
    }

    // partimos do pressuposto que o id nunca irá ultrapassar a parte do numero
    private static String increaseId(String id){
        String nextId = id;
        char firstChar = nextId.charAt(0);
        nextId = nextId.substring(1);
        int numPart = Integer.parseInt(nextId);
        nextId = firstChar + fillZeros(numPart+1,SIZE_NUMBER_PART_ID);
        return nextId;
    }

    private static String fillZeros(int number, int numberOfDigits){
        String numb = String.valueOf(number);
        String newId = "";
        if(numb.length()>numberOfDigits) return "";
        for(int n=numb.length(); n<numberOfDigits; n++){
            newId += "0";
        }return newId += String.valueOf(number);
    }

    private static void connect() throws SQLException {
        try {
            con = DriverManager.getConnection(URL, username, password);
        } catch (SQLException sqlex) {
            System.out.println("Erro : " + sqlex.getMessage());
        }
    }

    private static void closeConnection() throws SQLException {
        try {
            // free the resources of the ResultSet
            if (rs != null) rs.close();
            // free the resources of the Statement
            if (stmt != null) stmt.close();
            // close connection
            if (con != null) con.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void exampleTest() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select nome, modelo, marca, localizacao from ACTIVO where estado = '1';");
            App.printTable(rs,4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static void checkRestrictionActivohierarchy() {

    }

    public static void checkRestrictionIntervencaoDtFim() {

    }

    public static void checkRestrictionIntervencao() {

    }

    public static void checkRestrictionVcomercial() {

    }

    public static void checkEquipasMin2Elements() {
    }

    public static void checkRestrictionConflictGestaoMan() {

    }

    //colocar marca e modelo a null se tal n for especificado ou ter 2 metodos, um com marca e modelo e outro sem
    public static void novoActivo(String nome) throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            //INSERT INTO ACTIVO(id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa)VALUES ('a0001','cena1','1','2021-02-02',NULL,NULL,'ali','a0001',3,1,2)
            pstmt.setString(1, increaseId(maxIdActivo()));    //ident reserva
            pstmt.setString(2,nome);
            pstmt.setString(3,estado);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // FECHO E VOLTO A LIGAR A CON/LIMPAR RECURSOS
    private static String maxIdActivo() throws SQLException {
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT MAX(id) FROM ACTIVO");
        rs.next();
        return rs.getString(1);
    }

    public static void substituirElem() {

    }

    public static void activoForaServico() {

    }

    public static void custoTotalActivo() {

    }

    public static void querrie2d() {

    }

    public static void querrie2e() {

    }

    public static void querrie3c() {

    }

    public static void querrie3d() {

    }



}
