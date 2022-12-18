#ifndef SETTINGS_H
#define SETTINGS_H

class Settings {
 public:
  static Settings& get() {
    static Settings instance;
    return instance;
  }

 private:
  Settings() {}
  ~Settings() {}
  Settings(const Settings&);
  Settings& operator=(const Settings&);
};

#endif  // SETTINGS_H
