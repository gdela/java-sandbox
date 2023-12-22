#!/bin/zsh

function turbooff() {
  echo 1 | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo
}

function turboon() {
  echo 0 | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo
}

function shieldcpu() {
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

function unshieldcpu() {
  sudo cset shield -r
  for i in /sys/devices/system/cpu/cpu?; do
      echo "powersave" | sudo tee $i/cpufreq/scaling_governor
  done
  for i in /sys/devices/system/cpu/cpu?; do
    echo -n "cpu $i: "
    cat $i/cpufreq/scaling_governor
  done
}

function shieldirq() {
  sudo service irqbalance stop
  rm -rf /tmp/irq-original-affinity
  mkdir -p /tmp/irq-original-affinity/individual

  shielded_dec=0
  for i in $(echo $1 | tr "," "\n"); do
    shielded_dec=$((shielded_dec + 1<<i))
  done
  unshielded_dec=$((255 ^ shielded_dec))
  unshielded_hex=$(printf "%x\n" "unshielded_dec")

  cp /proc/irq/default_smp_affinity /tmp/irq-original-affinity/default
  echo "$unshielded_hex" | sudo tee /proc/irq/default_smp_affinity

  # shellcheck disable=SC2045
  for irq in $(ls /proc/irq); do
    file=/proc/irq/$irq/smp_affinity
    if [ -f "$file" ]; then
      curr_hex=$(cat "$file")
      curr_dec=$(printf "%d\n" "0x$curr_hex")
      new_dec=$((curr_dec & unshielded_dec))
      if ((new_dec == 0)); then
        new_dec=$((unshielded_dec))
      fi
      new_hex=$(printf "%x\n" "$new_dec")

      cp "$file" /tmp/irq-original-affinity/individual/$irq
      echo "$new_hex" | sudo tee "$file"
    fi
  done
}

function unshieldirq() {
  sudo service irqbalance start
  sudo cp /tmp/irq-original-affinity/default /proc/irq/default_smp_affinity
  # shellcheck disable=SC2045
  for irq in $(ls /tmp/irq-original-affinity/individual); do
    sudo cp /tmp/irq-original-affinity/individual/$irq /proc/irq/$irq/smp_affinity
  done
}

set -u
unsetopt bgnice
echo 0 | sudo tee /proc/sys/kernel/nmi_watchdog
echo 0 | sudo tee /proc/sys/kernel/perf_event_paranoid

export CP="-cp target/dependency/*:target/classes/:target/test-classes/"

export HUSH="-XX:-UsePerfData"

export GC_LOG="-Xlog:gc"
export GC_OFF="-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xms1G -Xmx1G -XX:+AlwaysPreTouch"
export GC_G1="-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -Xms1G -Xmx1G -XX:+AlwaysPreTouch"

export JIT_LOG="-XX:+PrintCompilation"
export JIT_STAT="-XX:+UnlockDiagnosticVMOptions -XX:+CITime"

export JMH="org.openjdk.jmh.Main"


