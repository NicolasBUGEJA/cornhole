#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include "FreeMono9pt7b.h"

#ifndef PSTR
 #define PSTR // Make Arduino Due happy
#endif

/************ CONSTANTS AND CONFIGURATION ************/

// DEBUG MODE
//TODO Improve debug mode..
const bool DEBUG = true;

// Program version
const String VERSION = "1.0.0";

// HC06 bluetooth module configuration
const int HC06_BAUDRATE = 9600;
const int HC06_PINS[2] = {4, 5}; // PINS on the arduino board
const String HC06_DEFAULT_NAME = "CORN HOLE 2 TURBO";
const String HC06_DEFAULT_CODE = "8013";
const char HC06_HEADER = '|';

// Init instance of HC06
SoftwareSerial hc06(HC06_PINS[0], HC06_PINS[1]);

// Matrix module configuration
const int MATRIX_PIN = 2;
const bool MATRIX_TEXT_WRAP = false;
const int MATRIX_BRIGHTNESS = 5;
const int MATRIX_TEXT_SIZE = 1;

// Init instance of Matrix
Adafruit_NeoMatrix matrix = Adafruit_NeoMatrix(32, 8, MATRIX_PIN,
  NEO_MATRIX_BOTTOM + NEO_MATRIX_RIGHT + NEO_MATRIX_ROWS + NEO_MATRIX_ZIGZAG,
  NEO_GRB + NEO_KHZ800);

// Matrix colors
const uint16_t MATRIX_COLORS[] = {
  matrix.Color(255, 255, 255), // WHITE
  matrix.Color(226, 63, 28), // RED
  matrix.Color(180, 255, 0), // ??
  matrix.Color(239, 223, 14) }; // ??

// Commands useful strings
const char CMD_SET_SCORE = 'S';
const char CMD_SET_SCORE_SEPARATOR = '-';
const char CMD_GET_VERSION = 'V';
const char CMD_SET_COLORS = 'C';
  
/************ STORE ************/
/** (variables used during
     the program lifetime) **/

String datas = ""; // Last command received from the android APP
int scoreLeft = 0;
int scoreRight = 0;
bool btReady = false;

int x_wait    = matrix.width();
int pass = 0;
String s_wait = "CORN HOLE 2 TURBO WAITING FOR PLAYERS...";

/************ MAIN FUNCTIONS ************/

/**
 * Setup the arduino on startup
 */
void setup() {
  Serial.begin(9600);
  Serial.println("STARTING CORN HOLE 2 TURBO");  
  
  Serial.println("-- INIT HC06 MODULE");  
  initHC06();
  
  Serial.println("-- INIT MATRIX MODULE");  
  initMatrix();
  
  Serial.println("-- SHOW WAITING TEXT");
  //showWaiting();
  delay(500);
  
  Serial.println("CORN HOLE 2 TURBO STARTED"); 
}

/**
 * Main function called during the arduino lifetime
 * Perpetual loop, waiting for new inputs...
 */
void loop() {
  // Write from Serial Monitor to HC06 (to send AT commands)
  if (Serial.available()){
    hc06.write(Serial.read());
  }  

  // Write from HCO6 to Serial Monitor and save new command received
  if (hc06.available() >= 2) { // If data is available to read    
    Serial.println("HC06 - Data available to read");
    delay(3);
    if (hc06.read() == HC06_HEADER)
    {      
      while(hc06.available()) { // While there is more to be read, keep reading        
        delay(3); // Delay needed to receive data from buffer
        datas += (char)hc06.read(); // Storing the command being received
        // (note : the HC06_HEADER has been 'removed' from buffer with the first read() call)
      }      
      Serial.println("HC06 - Datas received : " + datas);
      processDatas(datas); // Process the datas
    }
  }

  datas = ""; // Datas are emptied after being processed
}

/**
 * Process datas received from the HC06 bluetooth module
 */
void processDatas(String datas) {
  int dataLength = datas.length();
  if (dataLength >= 1) {
    char command = datas.charAt(0); // Retrieving the command
    Serial.println("-- HC06 - Command received : " + command);
    String commandDatas = ""; // Default command datas value
    if (dataLength > 1) { // Retrieving the command datas if exist
      commandDatas = datas.substring(1);
    }
    processCommand(command, commandDatas); // Process the command
  }
  else {
    Serial.println("-- HC06 - Datas length too short to be processed : " + dataLength);
  }
}

/**
 * Process command
 */
void processCommand(char command, String commandDatas) {
  switch (command) {
    case CMD_SET_SCORE: // Change the score displayed on the matrix
    processScoreCommand(commandDatas);
    break;
    case CMD_SET_COLORS: // Change the colors used on the matrix
    //TODO 
    break;
    case CMD_GET_VERSION: // Return version of the arduino program
    //TODO return VERSION to the app
    break;
  default:
    Serial.println("-- HC06 - Command not known for this version");
    //TODO return error code to the app
    break;
  }
}

/**
 * Process the SCORE command
 */
void processScoreCommand(String commandDatas) {
  int datasLength = commandDatas.length();
  int dashPosition = commandDatas.indexOf(CMD_SET_SCORE_SEPARATOR);
  if (datasLength < 3)
  {    
    Serial.println("SCORE - Command datas too short to be processed ; length : " + String(datasLength) + ", datas : " + commandDatas);
    //TODO return error code to the app
  }
  else if (dashPosition == -1)
  {
    Serial.println("SCORE - No separator found in command datas ; expected in string : " + CMD_SET_SCORE_SEPARATOR);
    //TODO return error code to the app
  }
  else if (dashPosition == 0 || dashPosition == (datasLength - 1))
  {
    Serial.println("SCORE - Separator is at the start or the end of the string while expected in between scores");
    //TODO return error code to the app
  }
  else
  {    
    String score[2] = { commandDatas.substring(0, dashPosition), commandDatas.substring(dashPosition + 1) }; // Retrieve score from the datas
    Serial.println("SCORE - Received ; left : " + score[0] + ", right : " + score[1]);
    showScore(score[0], score[1]); // Show score on the matrix
    // TODO return OK code and message to the app
  }
}

/************ MATRIX DISPLAY FUNCTIONS ************/

void initMatrix() {  
  matrix.begin();
  matrix.setTextWrap(MATRIX_TEXT_WRAP);
  matrix.setBrightness(MATRIX_BRIGHTNESS);
  matrix.setTextSize(MATRIX_TEXT_SIZE);
  matrix.setFont(&FreeMono9pt7b);
}

void showWaiting(){
  matrix.setBrightness(5);
  matrix.fillScreen(0);
  matrix.setCursor(x_wait, 7);
  int16_t x1, y1;
  uint16_t w, h;
  matrix.getTextBounds(s_wait, 0, 7, &x1, &y1, &w, &h);
  int zero = 0;
  //Serial.println(w);
 
  matrix.print(s_wait);
  Serial.println("xwait : " + (String)x_wait + " / w : " + (String)w + " -w : " + (String)(zero - (int)w));
  if(--x_wait < (zero - (int)w)) {
    x_wait = matrix.width();
    if(++pass >= 3) pass = 0;
    matrix.setTextColor(MATRIX_COLORS[pass]);
  }
  matrix.show();
  delay(100);
}

void drawCentreString(const String &buf, int x, int y)
{
    int16_t x1, y1;
    uint16_t w, h;
    matrix.getTextBounds(buf, x, y, &x1, &y1, &w, &h); //calc width of new string
    matrix.setCursor(x - w / 2, y);
    matrix.print(buf);
}

void showScore(String blue, String red) {
  matrix.clear();

  //matrix.fill(matrix.Color(239, 223, 14), 110, 2);
  //matrix.fill(MATRIX_COLORS[1], 142, 4);
  for(int i=112; i<114; i++) {
    matrix.setPixelColor(i, 239, 223, 14);
  }
  for(int i=142; i<144; i++) {
    matrix.setPixelColor(i, 239, 223, 14);
  }

  for(int i=110; i<112; i++) {
    matrix.setPixelColor(i, 226, 63, 28);
  }
  for(int i=144; i<146; i++) {
    matrix.setPixelColor(i, 226, 63, 28);
  }
  
  matrix.setTextColor(MATRIX_COLORS[3]);
    
  /*String bufferBlue = "";
  if(blue.length() < 2){
    bufferBlue = "0" + blue;
  }else{
    bufferBlue = blue;
  }*/
  drawCentreString(blue, 7, 7);

  //matrix.setTextColor(MATRIX_COLORS[3]);
  //drawCentreString("-", 18, 10);
  
  matrix.setTextColor(MATRIX_COLORS[1]);

  /*String bufferRed = "";
  if(red.length() < 2){
    bufferRed = "0" + red;
  }else{
    bufferRed = red;
  }*/
  drawCentreString(red, 25, 7);
  
  
  matrix.show();
}

/************ HC06 BLUETOOTH MODULE FUNCTIONS ************/

/**
 * Init HC06
 */
void initHC06() {  
  hc06.begin(HC06_BAUDRATE);
  if (DEBUG) {
    getHC06Version();
  }
  setHC06Name(HC06_DEFAULT_NAME);
  setHC06Pin(HC06_DEFAULT_CODE);
}

/**
 * Change HC06 name
 */
void setHC06Name(String name) {
  sendHC06Command("AT+NAME" + name);
}

/**
 * Change HC06 password
 */
void setHC06Pin(String pin) {
  sendHC06Command("AT+PIN" + pin);
}

/**
 * Display HC06 version
 * // TODO should we return the version instead and then display it ?
 */
void getHC06Version() {
  sendHC06Command("AT+VERSION");
}

/**
 * Send a serial command to HC06 module
 */
void sendHC06Command(String command) {
  String result = "";
  hc06.print(command); // Send command
  delay(2000); // Time needed for the HC06 to process the command
  if (hc06.available()) {
    while(hc06.available()) { // While there is more to be read, keep reading.
      delay(3); // Delay needed to receive data from buffer
      result += (char)hc06.read(); 
    }
  }
  delay(1000); // Nobody knows why we need to wait there.. 
  Serial.println(result);
}
