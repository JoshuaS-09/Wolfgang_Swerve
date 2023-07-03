package frc.robot;

public class Autonomous {
  private Drivetrain drivetrain;
  
  public double autoMaxPower = 0;
  public double driveSMin = 0.10;
  public double autoPower = 0;
  private boolean feildRelative = false;

  public Autonomous(Drivetrain drivetrain) {
    this.drivetrain = drivetrain;
  }

  public void autoDrive(double xSpeed, double ySpeed, double rotSpeed) {
    drivetrain.drive(xSpeed, ySpeed, rotSpeed, feildRelative);
  }

  public void driveStraight(double speed) {
    drivetrain.drive(speed, 0, 0, feildRelative);
  }

  public void crabDrive(double speed) {
    drivetrain.drive(0, speed, 0, feildRelative);
  }

  public void rotate(double speed) {
    drivetrain.drive(0, 0, speed, feildRelative);
  }

  public void chargeStation() {
    double error = drivetrain.robotRoll();
    double kP = (1 - driveSMin) / 20;
    // double kP = 0.1;
    if (Math.abs(drivetrain.navx.getRoll()) < 2) {
      autoMaxPower = 0;
      autoPower = 0;
    } else {
      autoMaxPower = 0.45; // Qualification match 17 20/05/2023 autoMaxPower = 0.60.
      autoPower = (driveSMin + kP * error) * autoMaxPower;
    }

    this.driveStraight(autoPower);
  }

  public void driveOff() {
    drivetrain.drive(0, 0, 0, feildRelative);
  }
}
