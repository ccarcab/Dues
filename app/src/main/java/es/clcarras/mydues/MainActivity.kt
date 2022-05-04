package es.clcarras.mydues

import android.os.Bundle
import android.view.Menu
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.clcarras.mydues.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            val navController = findNavController(R.id.nav_host_fragment)

            with(binding) {
                setSupportActionBar(bottomAppBar)
                fab.setOnClickListener {
                    navController.navigate(R.id.nav_new_due)
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar, menu)
        return true
    }

    fun getFab() : FloatingActionButton? = _binding?.fab
}