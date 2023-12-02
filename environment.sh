#!/bin/zsh

function turbooff() {
  echo 1 | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo
}

function turboon() {
  echo 0 | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo
}

function shield() {
  sudo cset shield -c $1 -k on
  sudo cset shield
  for i in $(echo $1 | tr "," "\n"); do
    echo "performance" | sudo tee /sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor
  done
  for i in /sys/devices/system/cpu/cpu?; do
    echo -n "cpu $i: "
    cat $i/cpufreq/scaling_governor
  done
}

function unshield() {
  sudo cset shield -r
  for i in /sys/devices/system/cpu/cpu?; do
      echo "powersave" | sudo tee $i/cpufreq/scaling_governor
  done
  for i in /sys/devices/system/cpu/cpu?; do
    echo -n "cpu $i: "
    cat $i/cpufreq/scaling_governor
  done
}

set -u
unsetopt bgnice
echo 0 | sudo tee /proc/sys/kernel/nmi_watchdog

export CP="-cp target/dependency/*:target/classes/:target/test-classes/"

export HUSH="-XX:-UsePerfData"

export GC_LOG="-Xlog:gc"
export GC_OFF="-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xms1G -Xmx1G -XX:+AlwaysPreTouch"
export GC_G1="-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Xms1G -Xmx1G -XX:+AlwaysPreTouch"

export JIT_LOG="-XX:+PrintCompilation"
export JIT_STAT="-XX:+UnlockDiagnosticVMOptions -XX:+CITime"

export JMH="org.openjdk.jmh.Main"


