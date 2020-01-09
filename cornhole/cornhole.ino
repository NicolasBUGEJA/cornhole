#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>
#include <Adafruit_NeoPixel.h>

#ifndef PSTR
 #define PSTR // Make Arduino Due happy
#endif

#define PIN 2

Adafruit_NeoMatrix matrix = Adafruit_NeoMatrix(32, 8, PIN,
  NEO_MATRIX_TOP     + NEO_MATRIX_LEFT +
  NEO_MATRIX_ROWS + NEO_MATRIX_ZIGZAG,
  NEO_GRB            + NEO_KHZ800);

const uint16_t colors[] = {
  matrix.Color(255, 255, 255), matrix.Color(255, 26, 0), matrix.Color(180, 255, 0), matrix.Color(0, 200, 255) };

int scoreBlue = 0;
int scoreRed = 0;

void showScore(String blue, String red) {
  matrix.begin();
  matrix.fillScreen(0);
  matrix.setTextWrap(false);
  matrix.setBrightness(20);
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
  // put your setup code here, to run once:
  

  showScore((String)scoreBlue, (String)scoreRed);
}



void loop() {
  // put your main code here, to run repeatedly:
  //delay(1000);

  //scoreBlue++;
  //showScore((String)scoreBlue, (String)scoreRed);

  //delay(1000);
  //scoreRed++;
  //showScore((String)scoreBlue, (String)scoreRed);
}
