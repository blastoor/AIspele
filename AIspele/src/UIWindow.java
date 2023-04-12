
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JOptionPane;


/**
 *
 * @author Ilmārs Štolcers
 */

class Board { // laukumu aprakstošā klase
    int[][] numbers; // laukuma rūtiņu reprezentācija ar vērtībām
    int firstPlayerScore; // pirmā spēlētāja tekošais punktu skaits
    int secondPlayerScore; // otrā spēlētāja tekošais punktu skaits
    boolean firstToMove; // norāda, kuram spēlētājam jāveic gājiens
    
    public Board(){ // laukuma konstruktors, kas piešķir sākuma vērtības
        numbers = new int[8][8];
        firstPlayerScore = 0;
        secondPlayerScore = 0;
        firstToMove = true;
    }
    
    public int evaluation(){ // heiristiskā novērtējuma funkcija
        int heuristicScore = 9*(firstPlayerScore - secondPlayerScore);
            // funkcijas matemātiskā daļa labākajiem stāvokļiem
        for(int i = 0; i < 8; i++){
            if(numbers[i][i] != 0) heuristicScore -= 1;
        } // funkcijas kosmētiskā daļa daudzveidīgākam spēles sākumam
        return heuristicScore;
    }
    
    public int checkPoints(int cellX, int cellY){ // pārbaude, cik punktu par gājienu
        int points = 0; // iegūto punktu daudzums sākumā
        int rowProduct = 1; // skaitļu reizinājums rindā
        int columnProduct = 1; // skaitļu reizinājums kolonnā
        for(int i = 0; i < 8; i++){ // sareizina visus skaitļus rindā
            rowProduct *= numbers[cellX][i];
        }
        for(int i = 0; i < 8; i++){ // sareizina visus skaitļus kolonnā
            columnProduct *= numbers[i][cellY];
        }
        if (rowProduct == -1) points++; // ja izpildās nosacījums rindā, iegūst punktu
        if (columnProduct == -1) points++; /* ja izpildās nosacījums kolonnā, 
                    iegūst punktu */
        return points;
    }
    
    public boolean isFull(){ // pārbaude, vai spēles laukums ir pilns
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if (numbers[i][j] == 0) return false; 
            } // atrod neaizpildītu rūtiņu (kur nav 1 vai -1)
        }
        return true;
    }
    
    public void makeMove(int cellX, int cellY){ // simulē gājiena veikšanu kodā
        if (firstToMove) { // pārbauda, vai pirmajam spēlētājam gājiens
            numbers[cellX][cellY] = 1; // ievieto pirmajam spēlētājam atbilstošo vērtību
            firstToMove = false; // maina, kam gājiens
            int pointsGained = checkPoints(cellX, cellY); // gājienā iegūtie punkti
            firstPlayerScore += pointsGained; // pirmā spēlētāja punktu atjaunošana
        }
        else { // analoģiski otrajam spēlētājam
            numbers[cellX][cellY] = -1;
            firstToMove = true;
            int pointsGained = checkPoints(cellX, cellY);
            secondPlayerScore += pointsGained;
        }
    }
    
    public void undoMove(int cellX, int cellY){ // atgriež stāvokli pirms gājiena
        if (firstToMove) { // pārbauda, vai pirmajam spēlētājam tagad ir gājiens
            int pointsGained = checkPoints(cellX, cellY); // cik ieguva punktus iepriekš
            secondPlayerScore -= pointsGained; // atņem iegūtos otrajam, kas gāja
            firstToMove = false; // maina, kam gājiens
            numbers[cellX][cellY] = 0; // izdzēš ierakstu laukumā
        }
        else { // alnaloģiski otrā gadījumā
            int pointsGained = checkPoints(cellX, cellY);
            firstPlayerScore -= pointsGained;
            firstToMove = true;
            numbers[cellX][cellY] = 0;
        }
    }
    
    public int[] findBestMove(boolean maximizing){ // atgriež labāko gājienu stāvoklī
        int bestHScore, currentHScore; // labākais un aplūkojamā stāvokļa heiristiskais vērtējums
        int[] bestMove = new int[2]; // labākā gājiena koordinātas
        if(maximizing){ // ja spēlētājs maksimizē
            bestHScore = -1000;
            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    if (numbers[i][j] != 0) continue; // atrod tukšās rūtiņas
                    makeMove(i, j); // veic katrā gājienu
                    currentHScore = findHScore(1, false);
                        // aplūkojamajam stāvoklim atrod heiristisko vērtējumu
                    if (currentHScore > bestHScore){
                        // ja vērtējums lielāks, atjaunina labāko gājienu
                        bestHScore = currentHScore;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                    undoMove(i, j); /* atgriež laukumu iepriekšējā stāvoklī, 
                        lai turpinātu pārmeklēšanu */
                }
            }
        }
        else{ // analoģiskas darbības, tikai tagad minimizē
            bestHScore = 1000;
            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    if (numbers[i][j] != 0) continue;
                    makeMove(i, j);
                    currentHScore = findHScore(1, true);
                    if (currentHScore < bestHScore){
                        bestHScore = currentHScore;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                    undoMove(i, j);
                }
            }
        }
        return bestMove; // atgriež labākā iespējamā gājiena koordinātas
    }
    
    public int findHScore(int depth, boolean maximizing){
        // rekursīva stāvokļu pārmeklēšana ar noteiktu dziļumu
        if(isFull() || depth == 4) return evaluation();
            // ja laukums pilns vai sasniegts dziļums, atgriež stāvokļa vertējumu
        int bestHScore, currentHScore;
        if(maximizing){ // ja maksimizētāja gājiens
            bestHScore = -1000;
            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    if (numbers[i][j] != 0) continue; // atrod iespējamu gājienu
                    makeMove(i, j); // veic gājienu
                    currentHScore = findHScore(depth+1, false);
                    /* atrod stāvokļa vērtējumu, rekursīvi pielietojot metodi
                    nākamajam dziļuma slānim, mainot to, ka nākamais ies minimizētājs*/
                    if (currentHScore > bestHScore){
                        // ja iegūtais vērtējums lielāks par agrāk atrasto, atjaunina
                        bestHScore = currentHScore;
                    }
                    undoMove(i, j); /* atgriež laukumu iepriekšējā stāvoklī, 
                        lai turpinātu pārmeklēšanu */
                }
            }
        }
        else{ // analoģiski, tikai minimizētājam
            bestHScore = 1000;
            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    if (numbers[i][j] != 0) continue;
                    makeMove(i, j);
                    currentHScore = findHScore(depth+1, true);
                    if (currentHScore < bestHScore){
                        bestHScore = currentHScore;
                    }
                    undoMove(i, j);
                }
            }
        }
        return bestHScore; // atgriež labāko vērtējumu 
    }
    
}

public class UIWindow extends javax.swing.JFrame { // galvenā loga klase
    
    Board gameBoard; //laukums kodā
    Graphics panel; // laukuma grafikas objekts
    boolean clickable; // karogs, kas norāda, vai spēlētājs drīkst veikt gājienu
    boolean computerMaximizing; /* karogs, kas norāda, vai dators veic pirmais 
            gājienu (maksimizē) */
    /**
     * Creates new form UIWindow
     */
    public UIWindow() {
        initComponents();
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_firstMove = new javax.swing.JLabel();
        m_firstPlayer = new javax.swing.JComboBox<>();
        m_startGame = new javax.swing.JButton();
        m_gameField = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        m_userPoints = new javax.swing.JLabel();
        m_computerPoints = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        m_firstMove.setText("Pirmo gājienu veiks:");

        m_firstPlayer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lietotājs", "Dators" }));

        m_startGame.setText("Sākt spēli!");
        m_startGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_startGameActionPerformed(evt);
            }
        });

        m_gameField.setPreferredSize(new java.awt.Dimension(321, 321));
        m_gameField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_gameFieldMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout m_gameFieldLayout = new javax.swing.GroupLayout(m_gameField);
        m_gameField.setLayout(m_gameFieldLayout);
        m_gameFieldLayout.setHorizontalGroup(
            m_gameFieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );
        m_gameFieldLayout.setVerticalGroup(
            m_gameFieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 321, Short.MAX_VALUE)
        );

        jLabel1.setText("Lietotāja (sarkanā) punkti:");

        jLabel2.setText("Datora (zilā) punkti:");

        m_userPoints.setText("0");

        m_computerPoints.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(m_gameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(m_startGame)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(m_computerPoints, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_firstMove)
                                        .addComponent(m_firstPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(75, 75, 75))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(m_userPoints, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap()))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_gameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_firstMove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_firstPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_startGame)
                        .addGap(90, 90, 90)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(m_userPoints))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(m_computerPoints))))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_startGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_startGameActionPerformed
        // metode, kas uzsāk spēli pēc pogas nospiešanas
        panel = m_gameField.getGraphics(); // piesaisti laukumu grafikas objektam
        panel.clearRect(0, 0, 321, 321); // notīra laukumu
        panel.setColor(Color.BLACK); // zīmē līnijas
        for (int i = 0; i < 9; i++){
            panel.drawLine(0, i*40, 320, i*40);
            panel.drawLine(i*40, 0, i*40, 320);
        }
        
        gameBoard = new Board(); // izveido tukšu laukumu kodā
        showPoints(); // atjaunina punktus uz 0
        if (m_firstPlayer.getSelectedIndex() == 0){ 
            computerMaximizing = false; /* reģistrē, vai dators būs maksimizētājs
                vai minimizētājs atkarībā no izvēlnes */
        }
        else{
            computerMaximizing = true;
            // ja dators maksimizē, izsauc pirmo gājienu
            int[] bestMove = gameBoard.findBestMove(computerMaximizing);
            drawMove(bestMove[0], bestMove[1]);
        }
        
        clickable = true; // ļauj spēlētājam veikt gājienu
    }//GEN-LAST:event_m_startGameActionPerformed

    private void m_gameFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_gameFieldMouseReleased
        // metode, kas veic lietotāja gājienu nospiestajā rūtiņā
        if (!clickable) return; // aizsardzība pret klikšķiem nepareizā brīdī
        int cellX = evt.getX() / 40; // aprēķina rūtiņas koordinātas
        int cellY = evt.getY() / 40;
        if (gameBoard.numbers[cellX][cellY] == 0){ // ja rūtiņa ir tukša
            drawMove(cellX, cellY); // attēlo lietotāja gājienu
            showPoints(); // atjaunina punktus
            if(gameBoard.isFull()) { // beidz spēli, ja pilns laukums
                clickable = false;
                endMessage();
                return;
            }
            clickable = false; // neļauj spēlētājam veikt klikšķus datora gājienā

            int[] bestMove = gameBoard.findBestMove(computerMaximizing); 
                // iegūst labāko datora gājienu
            drawMove(bestMove[0], bestMove[1]); // attēlo datora gājienu
            showPoints();// atjaunina punktus
            if(gameBoard.isFull()) { // beidz spēli, ja pilns laukums
                endMessage();
                return;
            }
            clickable = true; // atļauj spēlētājam veikt gājienu
        }
    }//GEN-LAST:event_m_gameFieldMouseReleased
    
    
    public void drawMove(int cellX, int cellY){ // veic un zīmē izvēlēto gājienu
        if (gameBoard.firstToMove) { // pārbauda, vai pirmais spēlētājs veica gājienu
            gameBoard.makeMove(cellX, cellY); // veic gājienu formāli laukuma datos
            if(!computerMaximizing) panel.setColor(Color.red); /* izvēlas pareizo
                krāsu atkarībā no tā, vai dators ir pirmais spēlētājs */
            else panel.setColor(Color.blue);
            panel.fillOval(cellX*40+5, cellY*40+5, 30, 30); // uzzīmē kauliņu
        }
        else { // analoģiski, tikai otrais spēlētājs veic gājienu
            gameBoard.makeMove(cellX, cellY);
            if(!computerMaximizing) panel.setColor(Color.blue);
            else panel.setColor(Color.red);
            panel.fillOval(cellX*40+5, cellY*40+5, 30, 30);
        }
    }
    
    public void showPoints(){ // atjaunina punktu vērtības attiecīgajos teksta laukos
        if(!computerMaximizing){ // pārbauda, vai dators ir otrais spēlētājs
            m_userPoints.setText(String.valueOf(gameBoard.firstPlayerScore));
            m_computerPoints.setText(String.valueOf(gameBoard.secondPlayerScore));
        }
        else{ // pārbauda, vai dators ir pirmais spēlētājs
            m_userPoints.setText(String.valueOf(gameBoard.secondPlayerScore));
            m_computerPoints.setText(String.valueOf(gameBoard.firstPlayerScore));
        }
    }
    
    public void endMessage(){ // izvada spēles beigu paziņojuma logu atkarībā no punktu skaita
        if(computerMaximizing){ // izvēlas gadījumu, ja dators ir pirmais spēlētājs
            if(gameBoard.firstPlayerScore > gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Dators uzvar!");
            if(gameBoard.firstPlayerScore == gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Neizšķirts!");
            if(gameBoard.firstPlayerScore < gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Lietotājs uzvar!");
        }
        else{ //izvēlas otru gadījumu
            if(gameBoard.firstPlayerScore > gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Lietotājs uzvar!");
            if(gameBoard.firstPlayerScore == gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Neizšķirts!");
            if(gameBoard.firstPlayerScore < gameBoard.secondPlayerScore)
                JOptionPane.showMessageDialog(null, "Dators uzvar!");
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UIWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UIWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UIWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UIWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UIWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel m_computerPoints;
    private javax.swing.JLabel m_firstMove;
    private javax.swing.JComboBox<String> m_firstPlayer;
    private javax.swing.JPanel m_gameField;
    private javax.swing.JButton m_startGame;
    private javax.swing.JLabel m_userPoints;
    // End of variables declaration//GEN-END:variables
}
