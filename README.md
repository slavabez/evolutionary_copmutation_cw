# Evolutionary Computation final year coursework

The task was to create an evolutionary algorithm capable of creating instances of Robocode robots and training them against a 'sitting duck', a stationary robot.

Robots were randomly generated, taken to the fight, evaluated against the fitness function and cross-bred (and sometimes mutated).

The end result, ideally, would be a robot that has 'learned' to successfully defeat the opponent.

Every robot generated looked like the following code:

```java
public class KB_<GEN_#>_<ID_#> extends AdvancedRobot {
    public void run() {
        this.setAdjustGunForRobotTurn(true);
        this.setColors(Color.RED, Color.BLACK, Color.WHITE);
        while (true) {
            this.turnGunRight(Double.POSITIVE_INFINITY);
        }
    }
    public void onScannedRobot(ScannedRobotEvent e){
        this.setFire(<CHROMOSOME #1>);
        this.setAhead(<CHROMOSOME #2>);
        this.setTurnLeft(<CHROMOSOME #3>);
    }
    public void onHitByBullet(HitByBulletEvent e){
        this.setTurnLeft(<CHROMOSOME #4>);      
        this.setBack(<CHROMOSOME #5>);
    }
}
```

The five chromosomes were used in the fitness function to determine the fitter robots. if you'd like to know more about this let me know at bezgachev@gmail.com.

Surprisingly, I ended up getting a pretty good grade for this, around 85%.

