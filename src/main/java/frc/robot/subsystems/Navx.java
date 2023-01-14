package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Navx extends SubsystemBase {
  private AHRS ahrs = new AHRS();

  public Navx() {
    ahrs.reset();
    ahrs.calibrate();
  }

  public double getPitch() {
    return ahrs.getPitch();
  }
}
