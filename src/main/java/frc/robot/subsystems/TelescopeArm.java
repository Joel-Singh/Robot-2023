package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class TelescopeArm extends SubsystemBase {
  private final int MOTOR_ID = 4;
  CANSparkMax motor = new CANSparkMax(MOTOR_ID, MotorType.kBrushless);

  public TelescopeArm() {
    final boolean IS_INVERTED = true;
    motor.setInverted(IS_INVERTED);
  }

  public void setSpeed(double speed) {
    motor.set(speed);
  }
}
