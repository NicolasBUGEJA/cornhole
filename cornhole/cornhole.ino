#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>

//Serial port
#define HEADER        '|'
#define MESSAGE_BYTES  2  // the total bytes in a message
char state = 'S';

#ifndef PSTR
 #define PSTR // Make Arduino Due happy
#endif

#define PIN 2

SoftwareSerial hc06(4, 5);

Adafruit_NeoMatrix matrix = Adafruit_NeoMatrix(32, 8, PIN,
  NEO_MATRIX_TOP     + NEO_MATRIX_LEFT +
  NEO_MATRIX_ROWS + NEO_MATRIX_ZIGZAG,
  NEO_GRB            + NEO_KHZ800);

const uint16_t colors[] = {
  matrix.Color(255, 255, 255), matrix.Color(255, 26, 0), matrix.Color(180, 255, 0), matrix.Color(0, 200, 255) };

int scoreBlue = 0;
int scoreRed = 0;

void showScore(String blue, String red) {
  matrix.fillScreen(0);
  matrix.setTextWrap(false);
  matrix.setBrightness(10);
  matrix.setTextSize(1);
  
  matrix.setTextColor(colors[3]);
  matrix.setCursor(1, 0);
  matrix.print(blue);

  matrix.setTextColor(colors[0]);
  matrix.setCursor(14, 0);
  matrix.print("-");

  matrix.setTextColor(colors[1]);
  matrix.setCursor(19, 0);
  matrix.print(red);
  matrix.show();
}

void setup() {
  hc06.begin(9600);
  matrix.begin();
  showScore((String)scoreBlue, (String)scoreRed);

  Serial.begin(9600);
  Serial.println("STARTING CORN HOLE 2 TURBO");  

  delay(500);
}



void loop() {
  char old_state = state;
  
  if (hc06.available() >= MESSAGE_BYTES) { // If data is available to read    
    Serial.println("Available");
    delay(3);
    if (hc06.read() == HEADER)
    {
      state = (char)hc06.read(); // read it and store it in val
    }
  }

  if(old_state != state){
    Serial.print("Received : ");
    Serial.println(state);
    switch (state) {
      case 'A':
        scoreBlue = 10;
        Serial.println("Blue team 10 points");
        break;
      case 'B':
        scoreBlue = 20;
        Serial.println("Blue team 20 points");
        break;
      case 'X':
        scoreRed = 10;
        Serial.println("Red team 10 points");
        break;
      case 'Y':
        scoreRed = 20;
        Serial.println("Red team 20 points");
        break;
      default://S
        Serial.println("resting");
        break;
    }
  
    showScore((String)scoreBlue, (String)scoreRed);
  }
}
