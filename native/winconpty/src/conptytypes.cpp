#include "conptytypes.h"

using namespace _winconpty_;

std::map<int, CONPTY*> Storage::conptysMap = std::map<int, CONPTY*>();