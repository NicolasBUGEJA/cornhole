#include <SoftwareSerial.h>

SoftwareSerial hc06(4, 5);

//Serial port
#define HEADER        '|'
#define MESSAGE_BYTES  2  // the total bytes in a message
char state = 'S';
// Parameters
/******************************************************************\
  PRIVATE FUNCTION: setup

  PARAMETERS:
  ~ void

  RETURN:
  ~ void

  DESCRIPTIONS:
  Initiate inputs/outputs

  \******************************************************************/
void setup() {
  Serial.begin(9600);
  Serial.println("STARTING CORN HOLE 2 TURBO");
  delay(500);
}
/******************************************************************\
  PRIVATE FUNCTION: loop

  PARAMETERS:
  ~ void

  RETURN:
  ~ void

  DESCRIPTIONS:
  Main Function of the code
  \******************************************************************/
void loop() {
  if (hc06.available() >= MESSAGE_BYTES) { // If data is available to read    
    delay(3);
    if (hc06.read() == HEADER)
    {
      state = (char)hc06.read(); // read it and store it in val
    }
  }
  Serial.print("Received : ");
  Serial.println(state);

  switch (state) {
    case 'A':
      blueScore("10");
      Serial.println("Blue team 10 points");
      break;
    case 'B':
      blueScore("20");
      Serial.println("Blue team 20 points");
      break;
    case 'X':
      redScore("10");
      Serial.println("Red team 10 points");
      break;
    case 'Y':
      redScore("20");
      Serial.println("Red team 20 points");
      break;
    default://S
      Stop();
      Serial.println("resting");
      break;
  }
}

void blueScore(String score) {
}
void redScore(String score) {
}
void Stop() {  
}
